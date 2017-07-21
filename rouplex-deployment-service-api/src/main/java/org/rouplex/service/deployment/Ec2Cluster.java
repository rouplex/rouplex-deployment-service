package org.rouplex.service.deployment;

import java.util.Map;
import java.util.Set;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
class Ec2Cluster extends Cluster<Ec2Host> {
    private String imageId;
    private HostType hostType;
    private String userData;
    private String networkId;
    private String subnetId;
    private String iamRole;
    private Map<String, String> tags;
    private Set<String> securityGroupIds;
    private String keyName;

    public Ec2Cluster() {
    }

    // internal creation
    Ec2Cluster(String clusterId, DeploymentConfiguration deploymentConfiguration, GeoLocation geoLocation, String imageId,
               HostType hostType, String userData, String networkId, String subnetId, String iamRole,
               Map<String, String> tags, Set<String> securityGroupIds, String keyName, Map<String, Ec2Host> hosts) {

        super(clusterId, geoLocation, deploymentConfiguration, hosts);

        this.imageId = imageId;
        this.hostType = hostType;
        this.userData = userData;
        this.networkId = networkId;
        this.subnetId = subnetId;
        this.iamRole = iamRole;
        this.tags = tags;
        this.securityGroupIds = securityGroupIds;
        this.keyName = keyName;
    }

    public HostType getHostType() {
        return hostType;
    }

    public void setHostType(HostType hostType) {
        this.hostType = hostType;
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
