package org.rouplex.service.deployment;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Ec2Host extends Host {
    public Ec2Host(String hostId, long startTimestamp, String privateIpAddress) {
        this.hostId = hostId;
        this.startTimestamp = startTimestamp;
        this.privateIpAddress = privateIpAddress;
    }
}
