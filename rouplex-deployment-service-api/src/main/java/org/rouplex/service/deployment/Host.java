package org.rouplex.service.deployment;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A generic host instance. Concrete instances are Ec2Host, GcHost etc ...
 *
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Host {
    protected String hostId;
    protected String clusterId;
    protected long startTimestamp;
    protected String privateIpAddress;

    // Following fields get updated by various threads, hence using Atomic's properties for memory barriers
    protected AtomicReference<String> publicIpAddress = new AtomicReference<>();
    protected AtomicLong lastDeploymentStateUpdateTimestamp = new AtomicLong(System.currentTimeMillis());
    protected AtomicReference<DeploymentState> deploymentState = new AtomicReference<>();

    public Host() {
    }

    Host(String hostId, String clusterId, long startTimestamp, String privateIpAddress) {
        this.hostId = hostId;
        this.clusterId = clusterId;
        this.startTimestamp = startTimestamp;
        this.privateIpAddress = privateIpAddress;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getLastDeploymentStateUpdateTimestamp() {
        return lastDeploymentStateUpdateTimestamp.get();
    }

    public void setLastDeploymentStateUpdateTimestamp(long lastDeploymentStateUpdateTimestamp) {
        this.lastDeploymentStateUpdateTimestamp.set(lastDeploymentStateUpdateTimestamp);
    }

    public DeploymentState getDeploymentState() {
        return deploymentState.get();
    }

    public void setDeploymentState(DeploymentState deploymentState) {
        this.deploymentState.set(deploymentState);
    }

    public String getPrivateIpAddress() {
        return privateIpAddress;
    }

    public void setPrivateIpAddress(String privateIpAddress) {
        this.privateIpAddress = privateIpAddress;
    }

    public String getPublicIpAddress() {
        return publicIpAddress.get();
    }

    public void setPublicIpAddress(String publicIpAddress) {
        this.publicIpAddress.set(publicIpAddress);
    }
}
