package org.rouplex.service.deployment;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Deployment {
    private String id;
    private DeploymentConfiguration deploymentConfiguration;

    // Following fields get updated by various threads synchronized using this instance
    private final Set<String> clusterIds = new HashSet<>();

    public Deployment() {
    }

    Deployment(String id, DeploymentConfiguration deploymentConfiguration) {
        this.id = id;
        this.deploymentConfiguration = deploymentConfiguration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeploymentConfiguration getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    public void setDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = deploymentConfiguration;
    }

    public Set<String> getClusterIds() {
        return clusterIds;
    }
}
