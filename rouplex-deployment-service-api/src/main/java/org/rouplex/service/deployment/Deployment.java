package org.rouplex.service.deployment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Deployment {
    private String deploymentId;
    private DeploymentConfiguration deploymentConfiguration;

    // Following fields get updated by various threads synchronized using this instance
    private final Map<String, CloudProvider> clusterIds = new HashMap<>();

    public Deployment() {
    }

    Deployment(String deploymentId, DeploymentConfiguration deploymentConfiguration) {
        this.deploymentId = deploymentId;
        this.deploymentConfiguration = deploymentConfiguration;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public DeploymentConfiguration getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    public void setDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = deploymentConfiguration;
    }

    public Map<String, CloudProvider> getClusterIds() {
        return clusterIds;
    }
}
