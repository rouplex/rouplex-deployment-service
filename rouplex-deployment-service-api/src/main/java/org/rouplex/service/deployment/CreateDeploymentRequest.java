package org.rouplex.service.deployment;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class CreateDeploymentRequest {
    private DeploymentConfiguration deploymentConfiguration;

    public DeploymentConfiguration getDeploymentConfiguration() {
        return deploymentConfiguration;
    }

    public void setDeploymentConfiguration(DeploymentConfiguration deploymentConfiguration) {
        this.deploymentConfiguration = deploymentConfiguration;
    }
}
