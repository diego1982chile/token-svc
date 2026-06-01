package cl.dsoto.onboarding.resources;

import cl.dsoto.onboarding.OnboardingTrainViewService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@RequestScoped
@Produces(APPLICATION_JSON)
@Path("onboarding")
public class OnboardingResource {

    @Inject
    OnboardingTrainViewService trainViewService;

    @GET
    @Path("me/train")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getMyTrain(@Context SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return trainViewService.getTrainView(securityContext.getUserPrincipal().getName())
                .map(view -> Response.ok(view).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("{username}/train")
    @RolesAllowed("ADMIN")
    public Response getUserTrain(@PathParam("username") String username) {
        return trainViewService.getTrainView(username)
                .map(view -> Response.ok(view).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
