package org.rouplex.service.deployment;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.rouplex.commons.utils.TimeUtils;
import org.rouplex.commons.utils.ValidationUtils;
import org.rouplex.service.deployment.management.ManagementService;
import org.rouplex.service.deployment.management.UpdateHostStateRequest;
import org.rouplex.service.deployment.management.UpdateHostStateResponse;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class DeploymentServiceProvider implements DeploymentService, ManagementService, Closeable {
    private static final Logger logger = Logger.getLogger(DeploymentServiceProvider.class.getSimpleName());

    private static DeploymentServiceProvider deploymentService;
    public static DeploymentServiceProvider get() throws Exception {
        synchronized (DeploymentServiceProvider.class) {
            if (deploymentService == null) {
                deploymentService = new DeploymentServiceProvider();
            }

            return deploymentService;
        }
    }

    private final Map<Ec2Region, AmazonEC2> amazonEC2Clients = new HashMap<>();
    private final Map<String /* deploymentId (serviceName) */, Deployment> deployments = new HashMap<>();
    private final Map<String /* clusterId (reservationId) */, Ec2Cluster> ec2Clusters = new HashMap<>();
    private final Map<String /* hostId (instanceId) */, Host> hosts = new HashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    DeploymentServiceProvider() {
        startMonitoringHosts();
    }

    @Override
    public void createDeployment(String deploymentId, CreateDeploymentRequest request) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");
        ValidationUtils.checkNonNullArg(request, "createDeploymentRequest");

        Deployment deployment = new Deployment(deploymentId);
        if (deployments.putIfAbsent(deploymentId, deployment) != null) {
            throw new IllegalStateException(String.format("Deployment [%s] is already registered", deploymentId));
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
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");

        Deployment deployment = deployments.get(deploymentId);
        if (deployment == null) {
            throw new Exception(String.format("Deployment [%s] not found", deploymentId));
        }

        logger.fine(String.format("Destroying deployment [%s]", deploymentId));

        try {
            deployment.getClusterIds().parallelStream().forEach(cid -> {
                try {
                    destroyEc2Cluster(deploymentId, cid);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException re) {
            throw (Exception) re.getCause();
        }

        deployments.remove(deploymentId);
        logger.info(String.format("Destroyed deployment [%s]", deploymentId));
    }

    @Override
    public String createEc2Cluster(String deploymentId, Ec2ClusterDescriptor ec2ClusterDescriptor) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");
        ValidationUtils.checkNonNullArg(ec2ClusterDescriptor, "ec2ClusterDescriptor");

        Deployment deployment = deployments.get(deploymentId);
        if (deployment == null) {
            throw new Exception(String.format("Deployment [%s] not found", deploymentId));
        }

        logger.fine(String.format("Creating ec2 cluster for deployment [%s]", deploymentId));

        InstanceType instanceType = InstanceType.fromValue(ec2ClusterDescriptor.getHostType().toString());
        Collection<Tag> tags = ec2ClusterDescriptor.getTags().entrySet().parallelStream()
            .map(tag -> new Tag(tag.getKey(), tag.getValue()))
            .collect(Collectors.toCollection(ArrayList::new));

        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
            .withImageId(ec2ClusterDescriptor.getImageId())
            .withInstanceType(instanceType)
            .withMinCount(ec2ClusterDescriptor.getHostCount())
            .withMaxCount(ec2ClusterDescriptor.getHostCount())
            .withIamInstanceProfile(new IamInstanceProfileSpecification().withName(ec2ClusterDescriptor.getIamRole()))
            .withSubnetId(ec2ClusterDescriptor.getSubnetId()) // auto create this in a later impl
            .withSecurityGroupIds(ec2ClusterDescriptor.getSecurityGroupIds()) // auto create this in a later impl
            .withKeyName(ec2ClusterDescriptor.getKeyName())
            .withUserData(ec2ClusterDescriptor.getUserData())
            .withTagSpecifications(new TagSpecification().withResourceType(ResourceType.Instance).withTags(tags));

        Reservation reservation = getAmazonEc2Client(ec2ClusterDescriptor.getRegion())
            .runInstances(runInstancesRequest).getReservation();

        Map<String, Ec2Host> ec2ClusterHosts = reservation.getInstances().parallelStream()
            .collect(Collectors.toMap(Instance::getInstanceId,
                i -> new Ec2Host(i.getInstanceId(), i.getLaunchTime().getTime(), i.getPrivateIpAddress())));

        String clusterId = reservation.getReservationId();
        deployment.getClusterIds().add(clusterId);
        ec2Clusters.put(clusterId, new Ec2Cluster(clusterId, ec2ClusterDescriptor, ec2ClusterHosts));
        ec2ClusterHosts.values().parallelStream().forEach(h -> hosts.put(h.getHostId(), h));

        logger.info(String.format("Created ec2 cluster [%s] for deployment [%s]", clusterId, deploymentId));
        return clusterId;
    }

    @Override
    public Set<String> listEc2ClusterIds(String deploymentId) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");

        Deployment deployment = deployments.get(deploymentId);
        if (deployment == null) {
            throw new Exception(String.format("Deployment [%s] not found", deploymentId));
        }

        return deployment.getClusterIds();
    }

    @Override
    public Ec2Cluster getEc2Cluster(String deploymentId, String clusterId) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");
        ValidationUtils.checkNonNullArg(clusterId, "clusterId");

        Deployment deployment = deployments.get(deploymentId);
        if (deployment == null) {
            throw new Exception(String.format("Deployment [%s] not found", deploymentId));
        }

        if (!deployment.getClusterIds().contains(clusterId)) {
            throw new Exception(String.format("Deployment [%s] does not contain cluster [%s]", deploymentId, clusterId));
        }

        Ec2Cluster ec2Cluster = ec2Clusters.get(clusterId);
        Map<String, Ec2Host> ec2ClusterHosts = ec2Cluster.getEc2Hosts();

        Set<String> hostIds = ec2ClusterHosts.entrySet().parallelStream()
            .map(Map.Entry::getKey).collect(Collectors.toSet());

        for (Reservation reservation : getAmazonEc2Client(ec2Cluster.getEc2ClusterDescriptor().getRegion())
            .describeInstances(new DescribeInstancesRequest().withInstanceIds(hostIds)).getReservations()) {

            for (Instance instance : reservation.getInstances()) {
                ec2ClusterHosts.get(instance.getInstanceId()).setPublicIpAddress(instance.getPublicIpAddress());
            }
        }

        return ec2Cluster;
    }

    @Override
    public void destroyEc2Cluster(String deploymentId, String clusterId) throws Exception {
        ValidationUtils.checkNonNullArg(deploymentId, "deploymentId");
        ValidationUtils.checkNonNullArg(clusterId, "clusterId");

        Deployment deployment = deployments.get(deploymentId);
        if (deployment == null) {
            throw new Exception(String.format("Deployment [%s] not found", deploymentId));
        }

        if (!deployment.getClusterIds().contains(clusterId)) {
            throw new Exception(String.format("Deployment [%s] does not contain cluster [%s]", deploymentId, clusterId));
        }

        logger.fine(String.format("Destroying ec2 cluster [%s]", clusterId));

        Ec2Cluster ec2Cluster = ec2Clusters.get(clusterId);
        Set<String> hostIds = ec2Cluster.getEc2Hosts().keySet();

        // all instances are in same region
        AmazonEC2 amazonEC2 = getAmazonEc2Client(ec2Cluster.getEc2ClusterDescriptor().getRegion());

        amazonEC2.createTags(new CreateTagsRequest().withResources(hostIds)
            .withTags(new Tag().withKey("State").withValue("Stopped by Deployment Service")));

        amazonEC2.terminateInstances(new TerminateInstancesRequest().withInstanceIds(hostIds));

        // only remove upon success
        ec2Clusters.remove(clusterId);
        deployment.getClusterIds().remove(clusterId);
        logger.info(String.format("Destroyed ec2 cluster [%s]", clusterId));
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

    @Override
    public UpdateHostStateResponse updateHostState(String hostId, UpdateHostStateRequest request) throws Exception {
        ValidationUtils.checkNonNullArg(hostId, "hostId");

        Host host = hosts.get(hostId);
        if (host == null) {
            throw new Exception(String.format("Host [%s] not found", hostId));
        }

        logger.fine(String.format("Updating host [%s] with state [%s]", hostId, request.getDeploymentState()));

        host.setLastDeploymentStateUpdateTimestamp(System.currentTimeMillis());
        host.setDeploymentState(request.getDeploymentState());

        String leaseExpiration = TimeUtils.convertMillisToIsoInstant(getPaddedToHourTimestamp(host.getStartTimestamp()), 0);
        UpdateHostStateResponse response = new UpdateHostStateResponse();
        response.setLeaseExpirationIsoTime(leaseExpiration);

        logger.info(String.format("Updated host [%s] with state [%s]. New lease expiration communicated is [%s]",
            hostId, request.getDeploymentState(), leaseExpiration));

        return response;
    }

    /**
     * Calculate the next timestamp starting from now that is the first multiple number of hours on or after
     * markerTimestamp. This serves as a cost optimiser for instance duration on ec2, since amazon charges on a per
     * hour usage basis.
     *
     * @param markerTimestamp
     *          The timestamp serving as a marker for the full hour increments
     * @return  The calculated timestamp
     */
    private long getPaddedToHourTimestamp(long markerTimestamp) {
        long now = System.currentTimeMillis();
        long oneHourFromNow = now + 60 * 60 * 1000;
        if (markerTimestamp == 0) {
            markerTimestamp = now;
        }

        if (markerTimestamp > oneHourFromNow) {
            return markerTimestamp;
        }

        return now + (now - markerTimestamp) % (60 * 60 * 1000);
    }

    private AmazonEC2 getAmazonEc2Client(Ec2Region region) {
        synchronized (amazonEC2Clients) {
            AmazonEC2 amazonEC2Client = amazonEC2Clients.get(region);

            if (amazonEC2Client == null) {
                amazonEC2Client = AmazonEC2ClientBuilder.standard()
                    .withRegion(Regions.fromName(region.toString())).build();

                amazonEC2Clients.put(region, amazonEC2Client);
            }

            return amazonEC2Client;
        }
    }

    private void startMonitoringHosts() {
        executorService.submit((Runnable) () -> {

            while (!executorService.isShutdown()) {
                // we note timeStart since the loop may take time to execute
                long timeStart = System.currentTimeMillis();

                try {
                    ec2Clusters.values().parallelStream().forEach(this::monitorCluster);
                } catch (RuntimeException re) {
                    // ConcurrentModification ... will be retried in one minute anyway
                }

                // update leases once a minute (or less often occasionally)
                long waitMillis = timeStart + 60 * 1000 - System.currentTimeMillis();
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

    private void monitorCluster(Ec2Cluster ec2Cluster) {
        logger.fine(String.format("Monitoring ec2 cluster [%s]", ec2Cluster.getClusterId()));

        // all instances are in same region
        AmazonEC2 amazonEC2 = getAmazonEc2Client(ec2Cluster.getEc2ClusterDescriptor().getRegion());

        ec2Cluster.getEc2Hosts().values().parallelStream().forEach(h -> {
            // did we observe at least 10 minutes of silence from host?
            if (h.getLastDeploymentStateUpdateTimestamp() + 10 * 60 * 1000 < System.currentTimeMillis()) {
                // we observed 10 minutes of silence, but lets use up the 1 hour instance slot before terminating it
                // remove 5 minutes from the slot to grant it to the ec2 termination process
                if (getPaddedToHourTimestamp(h.getStartTimestamp()) - 5 * 60 * 1000 < System.currentTimeMillis()) {
                    tagAndTerminateEC2Instance(amazonEC2, h.getHostId());
                }
            }
        });
    }

    private void tagAndTerminateEC2Instance(AmazonEC2 amazonEC2, String instanceId) {
        logger.fine(String.format("Terminating ec2 instance [%s]", instanceId));

        amazonEC2.createTags(new CreateTagsRequest().withResources(instanceId)
            .withTags(new Tag().withKey("State").withValue("Stopped by Benchmark Orchestrator")));

        amazonEC2.terminateInstances(new TerminateInstancesRequest().withInstanceIds(instanceId));
        logger.info(String.format("Terminated ec2 instance [%s]", instanceId));
    }
}