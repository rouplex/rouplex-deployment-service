package org.rouplex.service.deployment;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Ec2Host extends Host {
    public Ec2Host() {
    }

    Ec2Host(String hostId, String clusterId, long startTimestamp, String privateIpAddress) {
        super(hostId, clusterId, startTimestamp, privateIpAddress);
    }
}
