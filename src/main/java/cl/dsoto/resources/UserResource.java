package cl.dsoto.resources;

import cl.dsoto.entities.User;
import cl.dsoto.services.impl.DefaultUserService;
import io.quarkus.logging.Log;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * Created by des01c7 on 12-12-19.
 */
@RequestScoped
@Produces(APPLICATION_JSON)
@Path("users")
@RolesAllowed({"ADMIN"})
public class UserResource {

    @Inject
    DefaultUserService defaultUserService;

    @GET
    @Path("/me")
    public String me(@Context SecurityContext securityContext) {
        return securityContext.getUserPrincipal().getName();
    }

    @GET
    public Response getAllUsers() {
        try {
            List<User> users = defaultUserService.getAllUsers();
            return Response.ok(users).build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @POST
    @Path("save")
    public Response createUser(User user) {
        try {
            User newUser = defaultUserService.saveUser(user);
            return Response.ok(newUser).build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @DELETE
    @Path("delete/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        try {
            defaultUserService.deleteUser(id);
            return Response.ok().build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @DELETE
    @Path("clear")
    public Response clear() {
        try {
            defaultUserService.clear();
            return Response.ok().build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }
}
