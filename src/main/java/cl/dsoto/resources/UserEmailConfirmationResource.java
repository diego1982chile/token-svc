package cl.dsoto.resources;

import cl.dsoto.model.ResendConfirmationRequest;
import cl.dsoto.services.UserService;
import io.quarkus.logging.Log;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@PermitAll
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@Path("users")
public class UserEmailConfirmationResource {

    @Inject
    UserService userService;

    @GET
    @Path("confirm-email")
    public Response confirmEmail(@QueryParam("token") String token) {
        try {
            userService.confirmEmail(token);
            return Response.ok(Map.of("message", "Correo confirmado")).build();
        }
        catch (IllegalArgumentException e) {
            Log.error(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Link de confirmacion invalido o expirado"))
                    .build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @POST
    @Path("resend-confirmation")
    public Response resendConfirmation(ResendConfirmationRequest request) {
        try {
            String email = request == null ? null : request.getEmail();
            userService.resendEmailConfirmation(email);
            return Response.ok(Map.of(
                    "message", "Si el correo existe y requiere confirmacion, enviaremos un nuevo link."
            )).build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

}
