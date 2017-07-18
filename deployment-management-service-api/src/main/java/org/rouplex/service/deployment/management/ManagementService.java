package org.rouplex.service.deployment.management;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * @author Andi Mullaraj (andimullaraj at gmail.com)
 */
@Path("/deployment/management")
public interface ManagementService {
    @PUT
    @Path("/hosts/{hostId}")
    UpdateHostStateResponse updateHostState(@PathParam("hostId") String hostId, UpdateHostStateRequest request) throws Exception;
}