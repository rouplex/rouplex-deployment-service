package org.rouplex.service.deployment;

import java.util.Map;

/**
 * A cluster represents a group of hosts optionally deployable and maintainable as a single unit.
 * 1. A cluster can not span geographic locations, or cloud providers.
 * 2. Its hosts are of the same exact characteristics.
 * 3. A cluster can define a common strategy to be implemented by a {@link DeploymentService} provider.
 * 4. A cluster belongs to one deployment only (to keep it simple for now), but it can be made to serve more than one
 *
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
class Cluster<H extends Host> {
    private String clusterId;
    private GeoLocation geoLocation;
    private DeploymentConfiguration deploymentConfiguration;

    // Following fields get updated by various threads synchronized using this instance
    private Map<String /* hostId (instanceId) */, H> hosts;

    public Cluster() {
    }

    // internal creation
    Cluster(String clusterId, GeoLocation geoLocation,
            DeploymentConfiguration deploymentConfiguration, Map<String, H> hosts) {
        this.clusterId = clusterId;
        this.geoLocation = geoLocation;
        this.deploymentConfiguration = deploymentConfiguration;
        this.hosts = hosts;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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

    public Map<String, H> getHosts() {
        return hosts;
    }

    public void setHosts(Map<String, H> hosts) {
        this.hosts = hosts;
    }
}
