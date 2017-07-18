package org.rouplex.service.deployment;

import java.util.Map;
import java.util.Set;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
public class Ec2ClusterDescriptor {
    protected Ec2Region region;
    protected Ec2HostType hostType;
    protected int hostCount;
    private String imageId;
    private String userData; // optional
    private String networkId;
    private String subnetId; // optional
    private String iamRole; // optional
    private Map<String, String> tags; // optional
    private Set<String> securityGroupIds;
    private String keyName; // optional

    public Ec2Region getRegion() {
        return region;
    }

    public void setRegion(Ec2Region region) {
        this.region = region;
    }

    public Ec2HostType getHostType() {
        return hostType;
    }

    public void setHostType(Ec2HostType hostType) {
        this.hostType = hostType;
    }

    public int getHostCount() {
        return hostCount;
    }

    public void setHostCount(int hostCount) {
        this.hostCount = hostCount;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getIamRole() {
        return iamRole;
    }

    public void setIamRole(String iamRole) {
        this.iamRole = iamRole;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public Set<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(Set<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }
}
