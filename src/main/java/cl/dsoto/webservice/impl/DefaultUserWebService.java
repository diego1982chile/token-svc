package cl.dsoto.webservice.impl;

import cl.dsoto.model.RegistrationRequest;
import cl.dsoto.model.ResendConfirmationRequest;
import cl.dsoto.model.Role;
import cl.dsoto.model.User;
import cl.dsoto.services.ActiveUserAlreadyExistsException;
import cl.dsoto.services.ConfigService;
import cl.dsoto.services.CypherService;
import cl.dsoto.services.UserService;
import cl.dsoto.webservice.UserWebService;
import io.quarkus.logging.Log;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Path("users")
@RolesAllowed({"ADMIN"})
public class DefaultUserWebService implements UserWebService {

    @Inject
    UserService userService;

    @Inject
    ConfigService configService;

    @Inject
    CypherService cypherService;

    @ConfigProperty(name = "token.issuer")
    String jwtIssuer;

    @ConfigProperty(name = "token.access-audiences")
    List<String> jwtAudiences;

    @GET
    @Path("/me")
    @Override
    public String me(@Context SecurityContext securityContext) {
        return securityContext.getUserPrincipal().getName();
    }

    @GET
    @Override
    public Response getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            users.forEach(user -> user.setPassword(null));
            return Response.ok(users).build();
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @POST
    @Path("register")
    @PermitAll
    @Override
    public Response register(RegistrationRequest request) {
        try {
            if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                    || request.getPassword() == null || request.getPassword().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            return Response.accepted(userService.registerUser(request.getEmail(), request.getPassword())).build();
        } catch (ActiveUserAlreadyExistsException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", "ACCOUNT_ALREADY_EXISTS"))
                    .build();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
        return Response.serverError().build();
    }

    @POST
    @Path("save")
    @Override
    public Response createUser(User user) {
        try {
            User newUser = userService.saveUser(user);
            newUser.setPassword(null);
            return Response.ok(newUser).build();
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @DELETE
    @Path("delete/{id}")
    @Override
    public Response deleteUser(@PathParam("id") String id) {
        try {
            userService.deleteUser(id);
            return Response.ok().build();
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @GET
    @Path("confirm-email")
    @PermitAll
    @Override
    public Response confirmEmail(@QueryParam("token") String token) {
        try {
            User user = userService.confirmEmail(token);
            String accessToken = generateAccessToken(user);
            return Response.ok(Map.of(
                    "message", "Correo confirmado",
                    "token", accessToken,
                    "access_token", accessToken,
                    "token_type", "Bearer",
                    "expires_in", 3600
            )).build();
        } catch (IllegalArgumentException e) {
            Log.error(e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Link de confirmacion invalido o expirado"))
                    .build();
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    private String generateAccessToken(User user) throws IOException {
        PrivateKey privateKey = configService.getPrivateKey();
        List<String> roles = user.getRoles().stream()
                .map(Role::getRolename)
                .toList();
        return cypherService.generateJWT(privateKey, user.getUsername(), roles, jwtIssuer, jwtAudiences);
    }

    @POST
    @Path("resend-confirmation")
    @PermitAll
    @Override
    public Response resendConfirmation(ResendConfirmationRequest request) {
        try {
            String email = request == null ? null : request.getEmail();
            userService.resendEmailConfirmation(email);
            return Response.ok(Map.of(
                    "message", "Si el correo existe y requiere confirmacion, enviaremos un nuevo link."
            )).build();
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }
}
