package org.rouplex.service.deployment;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class CreateEc2ClusterResponse {
    String clusterId;

    CreateEc2ClusterResponse(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
