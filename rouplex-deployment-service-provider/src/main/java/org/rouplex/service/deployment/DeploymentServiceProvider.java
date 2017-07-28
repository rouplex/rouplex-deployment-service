package org.rouplex.service.deployment;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.rouplex.commons.configuration.Configuration;
import org.rouplex.commons.configuration.ConfigurationManager;
import org.rouplex.commons.exceptions.NotFoundException;
import org.rouplex.commons.utils.TimeUtils;
import org.rouplex.commons.utils.ValidationUtils;
import org.rouplex.service.deployment.management.ManagementService;
import org.rouplex.service.deployment.management.UpdateHostStateRequest;
import org.rouplex.service.deployment.management.UpdateHostStateResponse;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class DeploymentServiceProvider implements DeploymentService, ManagementService, Closeable {
    public enum ConfigurationKey {
        HostShutdownEstimationMillis,
        MonitoringPeriodMillis,
    }

    private static final Logger logger = Logger.getLogger(DeploymentServiceProvider.class.getSimpleName());

    private static DeploymentServiceProvider deploymentService;
    public static DeploymentServiceProvider get() throws Exception {
        synchronized (DeploymentServiceProvider.class) {
            if (deploymentService == null) {
                ConfigurationManager configurationManager = new ConfigurationManager();

                configurationManager.putConfigurationEntry(
                    ConfigurationKey.HostShutdownEstimationMillis, 2 * 60_000 + ""); // 2 minutes

                configurationManager.putConfigurationEntry(
                    ConfigurationKey.MonitoringPeriodMillis, 60_000 + ""); // 1 minute

                deploymentService = new DeploymentServiceProvider(configurationManager.getConfiguration());
            }

            return deploymentService;
        }
    }

    private final Configuration configuration;
    private final Map<GeoLocation, AmazonEC2> amazonEc2Clients = new HashMap<>();
    private final ConcurrentMap<String /* deploymentId (serviceName) */, Deployment> deployments = new ConcurrentHashMap<>();
    private final ConcurrentMap<String /* clusterId (reservationId) */, Cluster<? extends Host>> clusters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String /* id (instanceId) */, Host> hosts = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    DeploymentServiceProvider(Configuration configuration) {
        this.configuration = configuration;
        startMonitoring();
    }

    @Override
    public void createDeployment(String deploymentId, CreateDeploymentRequest request) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");
        ValidationUtils.checkNonNullArg(request, "createDeploymentRequest");

        DeploymentConfiguration depolymentConfiguration = request.getDeploymentConfiguration();
        ValidationUtils.checkNonNullArg(depolymentConfiguration, "deploymentConfiguration");

        ValidationUtils.checkNonNegativeArg(depolymentConfiguration.getLostHostIntervalMillis(), "lostHostIntervalMillis");
        if (depolymentConfiguration.getLeaseExpirationDateTime() != null) {
            ValidationUtils.checkDateTimeString(depolymentConfiguration.getLeaseExpirationDateTime(), "leaseExpirationDateTime");
        }

        logger.fine(String.format("Creating deployment [%s]", deploymentId));

        Deployment deployment = new Deployment(deploymentId, depolymentConfiguration);
        if (deployments.putIfAbsent(deploymentId, deployment) != null) {
            throw new IllegalStateException(String.format(
                "Deployment [%s] is already registered", deploymentId));
        }

        logger.info(String.format("Created deployment [%s]", deploymentId));
    }

    @Override
    public Set<String> listDeploymentIds() throws Exception {
        return deployments.keySet();
    }

    @Override
    public Deployment getDeployment(String deploymentId) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");
        return deployments.get(deploymentId);
    }

    @Override
    public void destroyDeployment(String deploymentId) throws Exception {
        Deployment deployment = locateDeployment(deploymentId);
        logger.fine(String.format("Destroying deployment [%s]", deploymentId));
        destroyDeployment(deployment);
        logger.info(String.format("Destroyed deployment [%s]", deploymentId));
    }

    @Override
    public CreateEc2ClusterResponse createEc2Cluster(String deploymentId, CreateEc2ClusterRequest request) throws Exception {
        Deployment deployment = locateDeployment(deploymentId);

        ValidationUtils.checkNonNullArg(request, "createEc2ClusterRequest");
        ValidationUtils.checkNonNullArg(request.getRegion(), "region");
        ValidationUtils.checkNonNullArg(request.getImageId(), "imageId");
        ValidationUtils.checkNonNullArg(request.getHostType(), "hostType");
        ValidationUtils.checkNonNullArg(request.getHostCount(), "hostCount");
        ValidationUtils.checkNonNullArg(request.getSubnetId(), "subnetId");
        ValidationUtils.checkNonNullArg(request.getSecurityGroupIds(), "securityGroupIds");

        // deployment/cluster instances are updated atomically
        // in this case the lock prevents adding a cluster to a deployment potentially being destroyed
        synchronized (deployment) {
            if (!deployments.containsKey(deploymentId)) {
                // competing deployment delete
                throw new IllegalStateException(String.format("Deployment [%s] not found", deploymentId));
            }

            DeploymentConfiguration deploymentConfiguration = request.getDeploymentConfiguration() != null ?
                request.getDeploymentConfiguration() : deployment.getDeploymentConfiguration();

            ValidationUtils.checkNonNullArg(deploymentConfiguration, "deploymentConfiguration");

            if (deploymentConfiguration.getLeaseExpirationDateTime() != null) {
                try {
                    TimeUtils.convertIsoInstantToMillis(deploymentConfiguration.getLeaseExpirationDateTime());
                } catch (ParseException pe) {
                    throw new Exception(String.format("Unparsable leaseExpirationDateTime [%s]",
                        deploymentConfiguration.getLeaseExpirationDateTime()));
                }
            }

            logger.fine(String.format("Creating ec2 cluster for deployment [%s]", deploymentId));

            RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withImageId(request.getImageId())
                .withInstanceType(InstanceType.fromValue(request.getHostType().toString()))
                .withUserData(request.getUserData())
                .withMinCount(request.getHostCount())
                .withMaxCount(request.getHostCount())
                .withSubnetId(request.getSubnetId()) // auto create this in a later impl
                .withSecurityGroupIds(request.getSecurityGroupIds()) // auto create this in a later impl
                .withKeyName(request.getKeyName());

            if (request.getIamRole() != null) {
                runInstancesRequest.withIamInstanceProfile(
                    new IamInstanceProfileSpecification().withName(request.getIamRole()));
            }

            if (request.getTags() != null && !request.getTags().isEmpty()) {
                Collection<Tag> tags = request.getTags().entrySet().parallelStream()
                    .map(tag -> new Tag(tag.getKey(), tag.getValue()))
                    .collect(Collectors.toCollection(ArrayList::new));

                runInstancesRequest.withTagSpecifications(new TagSpecification()
                    .withResourceType(ResourceType.Instance).withTags(tags));
            }

            Reservation reservation = getAmazonEc2Client(request.getRegion())
                .runInstances(runInstancesRequest).getReservation();
            String clusterId = reservation.getReservationId();

            Map<String, Ec2Host> ec2ClusterHosts = reservation.getInstances().parallelStream()
                .collect(Collectors.toMap(Instance::getInstanceId,
                    i -> new Ec2Host(i.getInstanceId(), clusterId, i.getLaunchTime().getTime(), i.getPrivateIpAddress())));

            Ec2Cluster ec2Cluster = new Ec2Cluster(clusterId, deploymentConfiguration, request.getRegion(), request.getImageId(),
                request.getHostType(), request.getUserData(), request.getNetworkId(), request.getSubnetId(),
                request.getIamRole(), request.getTags(), request.getSecurityGroupIds(), request.getKeyName(), ec2ClusterHosts);

            deployment.getClusterIds().add(clusterId);
            clusters.put(clusterId, ec2Cluster);
            ec2ClusterHosts.values().parallelStream().forEach(h -> hosts.put(h.getId(), h));

            logger.info(String.format("Created ec2 cluster [%s] for deployment [%s]", clusterId, deploymentId));
            return new CreateEc2ClusterResponse(clusterId);
        }
    }

    @Override
    public Set<String> listEc2ClusterIds(String deploymentId) throws Exception {
        Deployment deployment = locateDeployment(deploymentId);

        // may throw ConcurrentModificationException, which is fine (and can be retried)
        return deployment.getClusterIds().parallelStream()
            .filter(cid -> {
                Cluster cluster = clusters.get(cid);
                return cluster != null && cluster.getCloudProvider() == CloudProvider.AMAZON_AWS;
            })
            .collect(Collectors.toSet());
    }

    @Override
    public <H extends Host> Cluster<H> getCluster(String deploymentId, String clusterId) throws Exception {
        Deployment deployment = locateDeployment(deploymentId);
        ValidationUtils.checkNonNullArg(clusterId, "clusterId");

        Cluster<H> cluster = (Cluster<H>) clusters.get(clusterId);
        if (cluster == null) {
            throw new NotFoundException(String.format("Cluster [%s] not found",clusterId));
        }

        if (!deployment.getClusterIds().contains(clusterId)) {
            throw new NotFoundException(String.format(
                "Deployment [%s] does not contain cluster [%s]", deploymentId, clusterId));
        }

        return cluster;
    }

    @Override
    public Ec2Cluster getEc2Cluster(String deploymentId, String clusterId) throws Exception {
        Cluster<? extends Host> cluster = getCluster(deploymentId, clusterId);
        if (cluster.getCloudProvider() != CloudProvider.AMAZON_AWS) {
            throw new IllegalStateException(String.format("Cluster [%s] is not ec2", clusterId));
        }

        return (Ec2Cluster) cluster;
    }

    @Override
    public void destroyEc2Cluster(String deploymentId, String clusterId) throws Exception {
        Deployment deployment = locateDeployment(deploymentId);
        Ec2Cluster ec2Cluster = getEc2Cluster(deploymentId, clusterId);

        logger.fine(String.format("Destroying ec2 cluster [%s] in deployment [%s]", clusterId, deploymentId));

        destroyCluster(ec2Cluster);

        // deployment/cluster instances are updated atomically
        // in this case the lock protects the Map instance inside the deployment
        // (no ConcurrentMap to keep DTO simple)
        synchronized (deployment) {
            deployment.getClusterIds().remove(ec2Cluster.getId());
        }

        logger.info(String.format("Destroyed ec2 cluster [%s] in deployment [%s]", clusterId, deploymentId));
    }

    @Override
    public UpdateHostStateResponse updateHostState(String hostId, UpdateHostStateRequest request) throws Exception {
        ValidationUtils.checkNonNullArg(hostId, "hostId");

        Host host = hosts.get(hostId);
        if (host == null) {
            throw new IllegalStateException(String.format("Host [%s] not found", hostId));
        }

        Cluster cluster = clusters.get(host.getClusterId());
        if (cluster == null) {
            throw new IllegalStateException(String.format(
                "Cluster [%s] for host [%s] not found", host.getClusterId(), hostId));
        }

        logger.fine(String.format("Updating host [%s] in cluster [%s] with state [%s]",
            hostId, host.getClusterId(), request.getDeploymentState()));

        host.setLastDeploymentStateUpdateTimestamp(System.currentTimeMillis());
        host.setDeploymentState(request.getDeploymentState());

        UpdateHostStateResponse response = new UpdateHostStateResponse();
        response.setLeaseExpirationDateTime(cluster.getDeploymentConfiguration().getLeaseExpirationDateTime());

        logger.info(String.format("Updated host [%s] in cluster [%s] with state [%s]. New lease expiration is [%s]",
            hostId, host.getClusterId(), request.getDeploymentState(), response.getLeaseExpirationDateTime()));

        return response;
    }

    @Override
    public void close() throws IOException {
        logger.fine("Closing instance");
        executorService.shutdown();
        synchronized (executorService) {
            executorService.notifyAll();
        }
        logger.info("Closed instance");
    }

    protected void startMonitoring() {
        executorService.submit((Runnable) () -> {
            while (!executorService.isShutdown()) {
                // we note timeStart since the loop may take time to execute
                long timeStart = System.currentTimeMillis();

                try {
                    deployments.values().parallelStream().forEach(this::monitorDeployment);
                } catch (ConcurrentModificationException cme) {
                    // In case a deployment gets added or removed concurrently -- will be retried in one minute anyway
                }

                // update leases once a minute (or less often occasionally)
                long waitMillis = timeStart + configuration.getAsInteger(ConfigurationKey.MonitoringPeriodMillis)
                    - System.currentTimeMillis();

                if (waitMillis > 0) {
                    try {
                        synchronized (executorService) {
                            executorService.wait(waitMillis);
                        }
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
        });
    }

    protected Deployment locateDeployment(String deploymentId) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");

        Deployment deployment = deployments.get(deploymentId);
        if (deployment == null) {
            throw new NotFoundException(String.format("Deployment [%s] not found", deploymentId));
        }

        return deployment;
    }

    protected AmazonEC2 getAmazonEc2Client(GeoLocation geoLocation) {
        synchronized (amazonEc2Clients) {
            AmazonEC2 amazonEc2Client = amazonEc2Clients.get(geoLocation);

            if (amazonEc2Client == null) {
                AmazonEC2ClientBuilder amazonEC2ClientBuilder = AmazonEC2ClientBuilder.standard()
                    .withRegion(Regions.fromName(geoLocation.toString()));

                // this block can be omitted since it used only to debug from local host
                String awsAccessKey = System.getenv("awsAccessKey");
                String awsSecretKey = System.getenv("awsSecretKey");
                if (awsAccessKey != null && awsSecretKey != null) {
                    amazonEC2ClientBuilder.withCredentials(new AWSCredentialsProvider() {
                        @Override
                        public AWSCredentials getCredentials() {
                            return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
                        }

                        @Override
                        public void refresh() {
                        }
                    });
                }

                amazonEc2Client = amazonEC2ClientBuilder.build();
                amazonEc2Clients.put(geoLocation, amazonEc2Client);
            }

            return amazonEc2Client;
        }
    }

    protected long getTimeMillis(String dateTime) {
        try {
            return TimeUtils.convertIsoInstantToMillis(dateTime);
        } catch (NullPointerException | ParseException e) {
            return 0;
        }
    }

    protected void monitorDeployment(Deployment deployment) {
        logger.fine(String.format("Monitoring deployment [%s]", deployment.getId()));

        String expiration = deployment.getDeploymentConfiguration().getLeaseExpirationDateTime();
        if (expiration != null && getTimeMillis(expiration) < System.currentTimeMillis()) {
            try {
                destroyDeployment(deployment);
            } catch (Exception e) {
                logger.info(String.format("Failed destroying deployment [%s]. Cause: [%s: %s]",
                    deployment.getId(), e.getClass().getSimpleName(), e.getMessage()));
            }

            return;
        }

        deployment.getClusterIds().parallelStream().forEach(cid -> monitorCluster(clusters.get(cid)));

        // Splitting the following statements to avoid ConcurrentModification
        Set<String> finishingClusterIds = deployment.getClusterIds().parallelStream()
            .filter(cid -> clusters.get(cid) == null)
            .collect(Collectors.toSet());

        synchronized (deployment) {
            finishingClusterIds.stream().forEach(cid -> deployment.getClusterIds().remove(cid));
        }
    }

    protected void destroyDeployment(Deployment deployment) throws Exception {
        logger.fine(String.format("Destroying deployment [%s]", deployment.getId()));

        synchronized (deployment) {
            String deploymentId = deployment.getId();

            try {
                deployment.getClusterIds().parallelStream().forEach(cid -> {
                    try {
                        destroyCluster(clusters.get(cid));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (RuntimeException re) {
                throw (Exception) re.getCause();
            }

            deployments.remove(deploymentId);
        }
    }

    protected void monitorCluster(Cluster<? extends Host> cluster) {
        logger.fine(String.format("Monitoring cluster [%s]", cluster.getId()));

        String expiration = cluster.getDeploymentConfiguration().getLeaseExpirationDateTime();
        if (expiration != null && getTimeMillis(expiration) < System.currentTimeMillis()) {
            try {
                destroyCluster(cluster);
            } catch (Exception e) {
                logger.info(String.format("Failed destroying cluster [%s]. Cause: [%s: %s]",
                    cluster.getId(), e.getClass().getSimpleName(), e.getMessage()));
            }

            return;
        }

        cluster.getHosts().values().parallelStream().forEach(h -> monitorHostInCluster(h, cluster));

        // Splitting the following statements to avoid ConcurrentModification
        Set<? extends Host> destroyingHosts = cluster.getHosts().values().parallelStream()
            .filter(h -> h.getFinishingTimestamp() != 0)
            .collect(Collectors.toSet());

        synchronized (cluster) {
            destroyingHosts.stream().forEach(h -> cluster.getHosts().remove(h.getId()));
        }
    }

    protected void destroyCluster(Cluster<? extends Host> cluster) throws Exception {
        synchronized (cluster) {
            cluster.setFinishingTimestamp(System.currentTimeMillis());
            Set<String> hostIds = cluster.getHosts().keySet();

            switch (cluster.getCloudProvider()) {
                case AMAZON_AWS:
                    // all instances are in same region
                    AmazonEC2 amazonEc2Client = getAmazonEc2Client(cluster.getGeoLocation());

                    amazonEc2Client.createTags(new CreateTagsRequest().withResources(hostIds)
                        .withTags(new Tag().withKey("State").withValue("Terminated by deployment service")));

                    amazonEc2Client.terminateInstances(new TerminateInstancesRequest().withInstanceIds(hostIds));
                    break;
                case GOOGLE_GC:
                    // todo
                    break;
            }

            hostIds.parallelStream().forEach(hosts::remove);
            clusters.remove(cluster.getId());
        }
    }

    protected void monitorHostInCluster(Host host, Cluster cluster) {
        logger.fine(String.format("Monitoring host [%s]", host.getId()));
        long lostHostIntervalMillis = cluster.getDeploymentConfiguration().getLostHostIntervalMillis();

        if (lostHostIntervalMillis != 0) {
            long lastHostUpdate = Math.max(host.getStartingTimestamp(), host.getLastDeploymentStateUpdateTimestamp());

            if (System.currentTimeMillis() > lastHostUpdate + lostHostIntervalMillis) {
                // we observed at least lostHostIntervalMillis millis of silence,
                // but lets use up the 1 hour instance slot before terminating it
                long now = System.currentTimeMillis();
                long slotEndMillis = now + ((now - host.getStartingTimestamp()) / 3600 + 1) * 3600;
                long destroyTimestamp = slotEndMillis - configuration.getAsInteger(ConfigurationKey.HostShutdownEstimationMillis);

                if (System.currentTimeMillis() > destroyTimestamp) {
                    destroyHostInCluster(host, cluster);
                    return;
                }
            }
        }

        if (host.getPublicIpAddress() == null) {
            switch (cluster.getCloudProvider()) {
                case AMAZON_AWS:
                    for (Reservation reservation : getAmazonEc2Client(cluster.getGeoLocation()).describeInstances(
                        new DescribeInstancesRequest().withInstanceIds(host.getId())).getReservations()) {

                        for (Instance instance : reservation.getInstances()) {
                            host.setPublicIpAddress(instance.getPublicIpAddress());
                        }
                    }
                    break;
                case GOOGLE_GC:
                    // todo
                    break;
            }
        }
    }

    protected void destroyHostInCluster(Host host, Cluster cluster) {
        logger.fine(String.format("Terminating host [%s] in cluster [%s]", host.getId(), cluster.getId()));

        host.setFinishingTimestamp(System.currentTimeMillis());

        if (cluster instanceof Ec2Cluster) {
            AmazonEC2 amazonEc2Client = getAmazonEc2Client(cluster.getGeoLocation());
            amazonEc2Client.createTags(new CreateTagsRequest().withResources(host.getId())
                .withTags(new Tag().withKey("State").withValue("Terminated by deployment service")));

            amazonEc2Client.terminateInstances(new TerminateInstancesRequest().withInstanceIds(host.getId()));
        }
        // else handle gc cluster ...

        hosts.remove(host.getId());
        logger.info(String.format("Terminated host [%s] in cluster [%s]", host.getId(), cluster.getId()));
    }
}