package org.rouplex.service.deployment;

import javax.ws.rs.*;
import java.util.Set;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
@Path("/deployment")
public interface DeploymentService {
    @POST
    @Path("/deployments/{deploymentId}")
    void createDeployment(@PathParam("deploymentId") String deploymentId,
                          CreateDeploymentRequest request) throws Exception;

    @GET
    @Path("/deployments")
    Set<String> listDeploymentIds() throws Exception;

    @GET
    @Path("/deployments/{deploymentId}")
    Deployment getDeployment(@PathParam("deploymentId") String deploymentId) throws Exception;

    @DELETE
    @Path("/deployments/{deploymentId}")
    void destroyDeployment(@PathParam("deploymentId") String deploymentId) throws Exception;

    @POST
    @Path("/deployments/{deploymentId}/ec2/clusters")
    CreateEc2ClusterResponse createEc2Cluster(@PathParam("deploymentId") String deploymentId,
                                              CreateEc2ClusterRequest request) throws Exception;

    @GET
    @Path("/deployments/{deploymentId}/ec2/clusters")
    Set<String> listEc2ClusterIds(@PathParam("deploymentId") String deploymentId) throws Exception;

    @GET
    @Path("/deployments/{deploymentId}/ec2/clusters/{clusterId}")
    Ec2Cluster getEc2Cluster(@PathParam("deploymentId") String deploymentId,
                             @PathParam("clusterId") String clusterId) throws Exception;

    @DELETE
    @Path("/deployments/{deploymentId}/ec2/clusters/{clusterId}")
    void destroyEc2Cluster(@PathParam("deploymentId") String deploymentId,
                           @PathParam("clusterId") String clusterId) throws Exception;

}