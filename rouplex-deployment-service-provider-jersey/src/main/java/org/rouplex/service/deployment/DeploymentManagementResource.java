package org.rouplex.service.deployment;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.glassfish.jersey.server.ResourceConfig;
import org.rouplex.service.deployment.management.ManagementService;
import org.rouplex.service.deployment.management.UpdateHostStateRequest;
import org.rouplex.service.deployment.management.UpdateHostStateResponse;

@Api(value = "Deployment Management Service", description = "Service offering cloud deployment functionality")
public class DeploymentManagementResource extends ResourceConfig implements ManagementService {

    @ApiOperation(value = "Update state from the host, then respond with new leaseExpiration")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success"),
        @ApiResponse(code = 500, message = "Error handling request")})
    @Override
    public UpdateHostStateResponse updateHostState(String hostId, UpdateHostStateRequest request) throws Exception {
        return DeploymentServiceProvider.get().updateHostState(hostId, request);
    }
}
