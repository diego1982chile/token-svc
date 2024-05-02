package cl.dsoto.resources;


import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import java.net.InetAddress;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * Created by root on 09-12-22.
 */
@RequestScoped
@Produces(APPLICATION_JSON)
@Path("healthcheck")
public class HealthcheckResource implements HealthCheck {

    @GET
    @Override
    public HealthCheckResponse call() {

        String ip;

        try {
            ip = InetAddress.getLocalHost().getHostAddress();;
        }
        catch (Exception e) {
            ip = "UNKNOWN HOST";
        }

        return HealthCheckResponse.named("Application started").up().withData("IP", ip).build();
    }


}
