package org.rouplex.service.deployment;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A cluster represents a group of hosts optionally deployable and maintainable as a single unit.
 * 1. A cluster can not span geographic locations, or cloud providers.
 * 2. Its hosts are of the same exact characteristics.
 * 3. A cluster can define a common strategy to be implemented by a {@link DeploymentService} provider.
 * 4. A cluster belongs to one deployment only (to keep it simple for now), but it can be made to serve more than one
 *
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Cluster<H extends Host> {
    private String id;
    private CloudProvider cloudProvider;
    private GeoLocation geoLocation;
    private DeploymentConfiguration deploymentConfiguration;

    protected long startingTimestamp;
    protected AtomicLong finishingTimestamp = new AtomicLong();

    // Following fields get updated by various threads synchronized using this instance
    private Map<String /* id (instanceId) */, H> hosts;

    public Cluster() {
    }

    // internal creation
    Cluster(String id, CloudProvider cloudProvider, GeoLocation geoLocation,
            DeploymentConfiguration deploymentConfiguration, Map<String, H> hosts) {
        this.id = id;
        this.cloudProvider = cloudProvider;
        this.geoLocation = geoLocation;
        this.deploymentConfiguration = deploymentConfiguration;
        this.hosts = hosts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(CloudProvider cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public DeploymentConfiguration getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    public void setDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = deploymentConfiguration;
    }

    public long getStartingTimestamp() {
        return startingTimestamp;
    }

    public void setStartingTimestamp(long startingTimestamp) {
        this.startingTimestamp = startingTimestamp;
    }

    public long getFinishingTimestamp() {
        return finishingTimestamp.get();
    }

    public void setFinishingTimestamp(long finishingTimestamp) {
        this.finishingTimestamp.set(finishingTimestamp);
    }

    public Map<String, H> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, H> hosts) {
        this.hosts = hosts;
    }
}
