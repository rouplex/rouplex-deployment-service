package org.rouplex.service.deployment;

/**
 * A generic host instance. Concrete instances are Ec2Host, GcHost etc ...
 *
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Host {
    protected String hostId;
    protected long startTimestamp;
    protected long lastDeploymentStateUpdateTimestamp;
    protected DeploymentState deploymentState;
    protected String privateIpAddress;
    protected String publicIpAddress;

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getLastDeploymentStateUpdateTimestamp() {
        return lastDeploymentStateUpdateTimestamp;
    }

    public void setLastDeploymentStateUpdateTimestamp(long lastDeploymentStateUpdateTimestamp) {
        this.lastDeploymentStateUpdateTimestamp = lastDeploymentStateUpdateTimestamp;
    }

    public DeploymentState getDeploymentState() {
        return deploymentState;
    }

    public void setDeploymentState(DeploymentState deploymentState) {
        this.deploymentState = deploymentState;
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public String getPublicIpAddress() {
        return publicIpAddress;
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress = publicIpAddress;
    }
}
