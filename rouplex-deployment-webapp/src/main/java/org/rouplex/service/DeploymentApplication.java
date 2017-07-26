package org.rouplex.service;

import org.rouplex.platform.jersey.RouplexJerseyApplication;
import org.rouplex.service.deployment.DeploymentManagementResource;
import org.rouplex.service.deployment.DeploymentResource;
import org.rouplex.service.deployment.DeploymentServiceProvider;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

/**
 * This is the webapp, or the main jersey {@link javax.ws.rs.core.Application} which binds all the jersey resources.
 * The container searches for the {@link ApplicationPath} annotation and instantiates an instance of this class. It is
 * only in the constructor that we can bind (or add) resources to it, the jersey API does not allow for anything else.
 */
@ApplicationPath("/rest")
public class DeploymentApplication extends RouplexJerseyApplication {
    private static Logger logger = Logger.getLogger(DeploymentApplication.class.getSimpleName());

    public DeploymentApplication(@Context ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected void postConstruct() {
        bindRouplexResource(DeploymentResource.class, true);
        bindRouplexResource(DeploymentManagementResource.class, true);

        try {
            // instantiate early
            DeploymentServiceProvider.get();
        } catch (Exception e) {
            String errorMessage = String.format("Could not instantiate services. Cause: %s: %s",
                    e.getClass(), e.getMessage());

            logger.severe(errorMessage);
            getSwaggerBeanConfig().setDescription(errorMessage);
            return;
        }

        super.postConstruct();
    }

    @Override
    protected void initExceptionMapper() {
        register(new SevereExceptionMapper());
    }

    private class SevereExceptionMapper implements ExceptionMapper<Exception> {
        @Override
        public Response toResponse(Exception exception) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            exception.printStackTrace(new PrintStream(os));
            logger.severe("Stack trace: " + new String(os.toByteArray()));

            return Response.status(500).entity(new ExceptionEntity(exception)).build();
        }
    }
}
