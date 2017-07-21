package org.rouplex.service.deployment.management;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class UpdateHostStateResponse {
    static String ISO_INSTANT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    static String LEASE_END_ISO_INSTANT_EXAMPLE = "2017-12-31T10:00:00.000-0800";

    private String leaseExpirationDateTime;

    public String getLeaseExpirationDateTime() {
        return leaseExpirationDateTime;
    }

    public void setLeaseExpirationDateTime(String leaseExpirationDateTime) {
        this.leaseExpirationDateTime = leaseExpirationDateTime;
    }
}
