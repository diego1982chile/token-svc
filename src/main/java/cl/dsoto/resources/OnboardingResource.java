package cl.dsoto.resources;

import cl.dsoto.model.RegistrationStatusResponse;
import cl.dsoto.resources.dto.OnboardingTrainStep;
import cl.dsoto.resources.dto.OnboardingTrainView;
import cl.dsoto.services.OnboardingTrainViewService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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
    @Path("public/train")
    @PermitAll
    public Response getPublicTrain(@QueryParam("stage") String stage) {
        return Response.ok(trainViewService.getPublicTrainView(stage)).build();
    }

    @GET
    @Path("public/{registrationId}/status")
    @PermitAll
    public Response getPublicRegistrationStatus(@PathParam("registrationId") String registrationId) {
        return trainViewService.getTrainViewByRegistrationId(registrationId)
                .map(this::publicRegistrationStatus)
                .map(status -> Response.ok(status).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

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

    private RegistrationStatusResponse publicRegistrationStatus(OnboardingTrainView train) {
        boolean confirmed = train.currentStep() != OnboardingTrainStep.REGISTRATION;
        return new RegistrationStatusResponse(confirmed, train);
    }
}
