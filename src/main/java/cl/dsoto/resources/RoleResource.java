package cl.dsoto.resources;


import cl.dsoto.entities.Role;
import cl.dsoto.services.RoleService;
import io.quarkus.logging.Log;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


/**
 * Created by des01c7 on 12-12-19.
 */
@RequestScoped
@Produces(APPLICATION_JSON)
@Path("roles")
@RolesAllowed({"ADMIN"})
public class RoleResource {

    @Inject
    RoleService roleService;


    @GET
    public Response getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            return Response.ok(roles).build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @POST
    @Path("save")
    public Response createRole(Role role) {
        try {
            Role newRole = roleService.saveRole(role);
            return Response.ok(newRole).build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

    @PUT
    @Path("update")
    public Response updateRole(Role role) {
        try {
            Role newRole = roleService.updateRole(role);
            return Response.ok(newRole).build();
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
            roleService.deleteRole(id);
            return Response.ok().build();
        }
        catch (Exception e) {
            Log.error(e.getMessage());
        }
        return Response.serverError().build();
    }

}
