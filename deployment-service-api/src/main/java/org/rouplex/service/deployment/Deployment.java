package org.rouplex.service.deployment;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Deployment {
    private final String deploymentId;
    private final Set<String> clusterIds = new HashSet<>();

    public Deployment(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public Set<String> getClusterIds() {
        return clusterIds;
    }
}
