package org.rouplex.service.deployment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.glassfish.jersey.server.ResourceConfig;
import org.rouplex.service.deployment.management.ManagementService;
import org.rouplex.service.deployment.management.UpdateHostStateRequest;
import org.rouplex.service.deployment.management.UpdateHostStateResponse;

import java.util.Set;

@Api(value = "Deployment Service", description = "Service offering cloud deployment functionality")
public class DeploymentResource extends ResourceConfig implements DeploymentService, ManagementService {

    @ApiOperation(value = "Create a deployment (service to be deployed)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public void createDeployment(String deploymentId, CreateDeploymentRequest request) throws Exception {
        DeploymentServiceProvider.get().createDeployment(deploymentId, request);
    }

    @ApiOperation(value = "List all deployment ids (service names)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public Set<String> listDeploymentIds() throws Exception {
        return DeploymentServiceProvider.get().listDeploymentIds();
    }

    @ApiOperation(value = "Describe a (service) deployment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public Deployment getDeployment(String deploymentId) throws Exception {
        return DeploymentServiceProvider.get().getDeployment(deploymentId);
    }

    @ApiOperation(value = "Destroy a (service) deployment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public void destroyDeployment(String deploymentId) throws Exception {
        DeploymentServiceProvider.get().destroyDeployment(deploymentId);
    }

    @ApiOperation(value = "Create an ec2 regional cluster")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public CreateEc2ClusterResponse createEc2Cluster(String deploymentId, CreateEc2ClusterRequest request) throws Exception {
        return DeploymentServiceProvider.get().createEc2Cluster(deploymentId, request);
    }

    @ApiOperation(value = "List all deployed ec2 clusters for a (service) deployment")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public Set<String> listEc2ClusterIds(String deploymentId) throws Exception {
        return DeploymentServiceProvider.get().listEc2ClusterIds(deploymentId);
    }

    @ApiOperation(value = "Describe a deployed ec2 cluster")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public Ec2Cluster getEc2Cluster(String deploymentId, String clusterId) throws Exception {
        return DeploymentServiceProvider.get().getEc2Cluster(deploymentId, clusterId);
    }

    @ApiOperation(value = "Destroy a deployed ec2 cluster")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public void destroyEc2Cluster(String deploymentId, String clusterId) throws Exception {
        DeploymentServiceProvider.get().destroyEc2Cluster(deploymentId, clusterId);
    }

    @Override
    public UpdateHostStateResponse updateHostState(String hostId, UpdateHostStateRequest request) throws Exception {
        return DeploymentServiceProvider.get().updateHostState(hostId, request);
    }
}
