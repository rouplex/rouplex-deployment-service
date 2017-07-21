package org.rouplex.service.deployment.management;

import org.rouplex.service.deployment.DeploymentState;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class UpdateHostStateRequest {
    private DeploymentState deploymentState;

    public DeploymentState getDeploymentState() {
        return deploymentState;
    }

    public void setDeploymentState(DeploymentState deploymentState) {
        this.deploymentState = deploymentState;
    }
}
