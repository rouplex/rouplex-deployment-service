package org.rouplex.service.deployment;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A generic host instance. Concrete instances are Ec2Host, GcHost etc ...
 *
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Host {
    protected String id;
    protected String clusterId;
    protected long startingTimestamp;
    protected String privateIpAddress;

    // Following fields get updated by various threads, hence using Atomic's properties for memory barriers
    protected AtomicReference<String> publicIpAddress = new AtomicReference<>();
    protected AtomicLong finishingTimestamp = new AtomicLong();
    protected AtomicLong lastDeploymentStateUpdateTimestamp = new AtomicLong();
    protected AtomicReference<DeploymentState> deploymentState = new AtomicReference<>();

    public Host() {
    }

    Host(String id, String clusterId, long startingTimestamp, String privateIpAddress) {
        this.id = id;
        this.clusterId = clusterId;
        this.startingTimestamp = startingTimestamp;
        this.privateIpAddress = privateIpAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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
