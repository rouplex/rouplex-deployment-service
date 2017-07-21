package org.rouplex.service.deployment;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class DeploymentConfiguration {
    private long lostHostIntervalMillis;
    private boolean replaceLostHosts;
    private boolean terminateLostHosts;

    private String leaseExpirationDateTime;
    private boolean terminateExpiredHosts;

    public long getLostHostIntervalMillis() {
        return lostHostIntervalMillis;
    }

    public void setLostHostIntervalMillis(long lostHostIntervalMillis) {
        this.lostHostIntervalMillis = lostHostIntervalMillis;
    }

    public boolean isReplaceLostHosts() {
        return replaceLostHosts;
    }

    public void setReplaceLostHosts(boolean replaceLostHosts) {
        this.replaceLostHosts = replaceLostHosts;
    }

    public boolean isTerminateLostHosts() {
        return terminateLostHosts;
    }

    public void setTerminateLostHosts(boolean terminateLostHosts) {
        this.terminateLostHosts = terminateLostHosts;
    }

    public String getLeaseExpirationDateTime() {
        return leaseExpirationDateTime;
    }

    public void setLeaseExpirationDateTime(String leaseExpirationDateTime) {
        this.leaseExpirationDateTime = leaseExpirationDateTime;
    }

    public boolean isTerminateExpiredHosts() {
        return terminateExpiredHosts;
    }

    public void setTerminateExpiredHosts(boolean terminateExpiredHosts) {
        this.terminateExpiredHosts = terminateExpiredHosts;
    }
}
