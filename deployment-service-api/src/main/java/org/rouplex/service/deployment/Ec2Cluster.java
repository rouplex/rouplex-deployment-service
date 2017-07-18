package org.rouplex.service.deployment;

import java.util.Map;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
class Ec2Cluster {
    private final String clusterId;
    private final Ec2ClusterDescriptor ec2ClusterDescriptor;
    private final Map<String /* hostId (instanceId) */, Ec2Host> ec2Hosts;

    public Ec2Cluster(String clusterId, Ec2ClusterDescriptor ec2ClusterDescriptor, Map<String, Ec2Host> ec2Hosts) {
        this.clusterId = clusterId;
        this.ec2ClusterDescriptor = ec2ClusterDescriptor;
        this.ec2Hosts = ec2Hosts;
    }

    public String getClusterId() {
        return clusterId;
    }

    public Ec2ClusterDescriptor getEc2ClusterDescriptor() {
        return ec2ClusterDescriptor;
    }

    public Map<String, Ec2Host> getEc2Hosts() {
        return ec2Hosts;
    }
}
