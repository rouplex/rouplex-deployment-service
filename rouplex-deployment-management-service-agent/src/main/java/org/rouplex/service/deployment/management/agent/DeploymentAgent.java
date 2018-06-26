package org.rouplex.service.deployment.management.agent;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.util.EC2MetadataUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.rouplex.commons.configuration.Configuration;
import org.rouplex.commons.configuration.ConfigurationManager;
import org.rouplex.commons.utils.SecurityUtils;
import org.rouplex.commons.utils.TimeUtils;
import org.rouplex.commons.utils.ValidationUtils;
import org.rouplex.service.deployment.DeploymentState;
import org.rouplex.service.deployment.management.UpdateHostStateRequest;
import org.rouplex.service.deployment.management.UpdateHostStateResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class DeploymentAgent implements Closeable {
    public enum ConfigurationKey {
        DeploymentManagementUrl,
        DefaultLeaseMillis,
        ReportingPeriodMillis
    }

    private static final Logger logger = Logger.getLogger(DeploymentAgent.class.getSimpleName());
    private static final String JERSEY_CLIENT_READ_TIMEOUT = "jersey.config.client.readTimeout";
    private static final String JERSEY_CLIENT_CONNECT_TIMEOUT = "jersey.config.client.connectTimeout";

    private static DeploymentAgent deploymentAgent;

    public static DeploymentAgent get() throws Exception {
        synchronized (DeploymentAgent.class) {
            if (deploymentAgent == null) {
                ConfigurationManager configurationManager = new ConfigurationManager();

                configurationManager.putConfigurationEntry(ConfigurationKey.DeploymentManagementUrl,
                    "https://www.rouplex-demo.com/rest/deployment/management");

                configurationManager.putConfigurationEntry(
                    ConfigurationKey.DefaultLeaseMillis, 55 * 60_000 + ""); // 55 minutes

                configurationManager.putConfigurationEntry(
                    ConfigurationKey.ReportingPeriodMillis, 60_000 + ""); // once a minute

                deploymentAgent = new DeploymentAgent(configurationManager.getConfiguration());
            }

            return deploymentAgent;
        }
    }

    private final AtomicReference<DeploymentState> deploymentState = new AtomicReference<>(new DeploymentState());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Client jaxrsClient; // rouplex platform will provide a specific version of this soon

    private long leaseEnd;

    public DeploymentAgent(Configuration configuration) throws Exception {
        ValidationUtils.checkNonNullArg(configuration, "configuration");

        jaxrsClient = createJaxRsClient();
        leaseEnd = System.currentTimeMillis() + configuration.getAsInteger(ConfigurationKey.DefaultLeaseMillis);

        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (!executorService.isShutdown()) {
                    long timeStart = System.currentTimeMillis();

                    try {
                        logger.fine("Reporting deployment state and update leaseEnd");
                        UpdateHostStateRequest request = new UpdateHostStateRequest();
                        request.setDeploymentState(deploymentState.get());

                        UpdateHostStateResponse response = jaxrsClient
                            .target(configuration.get(ConfigurationKey.DeploymentManagementUrl))
                            .path("/hosts/" + EC2MetadataUtils.getInstanceId())
                            .request(MediaType.APPLICATION_JSON)
                            .put(Entity.entity(request, MediaType.APPLICATION_JSON), UpdateHostStateResponse.class);

                        leaseEnd = TimeUtils.convertIsoInstantToMillis(response.getLeaseExpirationDateTime());
                        logger.info(String.format("Reported deployment state and updated leaseEnd [%s]", leaseEnd));
                    } catch (Exception e) {
                        logger.warning(String.format("Failed to report deployment state and update leaseEnd. Cause: %s: %s",
                            e.getClass(), e.getMessage()));
                    }

                    // terminate self if beyond the lease
                    if (System.currentTimeMillis() > leaseEnd) {
                        logger.severe("Terminating self. Cause: Expired lease");

                        try {
                            AmazonEC2 amazonEC2Client = AmazonEC2ClientBuilder.standard()
                                .withRegion(EC2MetadataUtils.getEC2InstanceRegion()).build();

                            amazonEC2Client.createTags(new CreateTagsRequest()
                                .withResources(EC2MetadataUtils.getInstanceId())
                                .withTags(new Tag()
                                    .withKey("State")
                                    .withValue("Self terminated due to lease expiration")));

                            amazonEC2Client.terminateInstances(new TerminateInstancesRequest()
                                .withInstanceIds(EC2MetadataUtils.getInstanceId()));
                        } catch (RuntimeException re) {
                            logger.severe(String.format("Could not terminate self. Cause: %s: %s",
                                re.getClass(), re.getMessage()));
                        }
                    }

                    long waitMillis = timeStart + configuration.getAsInteger(ConfigurationKey.ReportingPeriodMillis)
                        - System.currentTimeMillis();

                    if (waitMillis > 0) {
                        synchronized (executorService) {
                            try {
                                executorService.wait(waitMillis);
                            } catch (InterruptedException ie) {
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        executorService.shutdownNow();
        synchronized (executorService) {
            executorService.notifyAll();
        }
    }

    public void setDeploymentState(DeploymentState deploymentState) {
        this.deploymentState.set(deploymentState);
        synchronized (executorService) {
            executorService.notifyAll();
        }
    }

    private Client createJaxRsClient() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(mapper);

        return ClientBuilder.newBuilder()
            .property(JERSEY_CLIENT_CONNECT_TIMEOUT, 12000)
            .property(JERSEY_CLIENT_READ_TIMEOUT, 12000)
            .register(provider)
            .sslContext(SecurityUtils.buildRelaxedSSLContext())
            .hostnameVerifier((s, sslSession) -> true)
            .build();
    }
}