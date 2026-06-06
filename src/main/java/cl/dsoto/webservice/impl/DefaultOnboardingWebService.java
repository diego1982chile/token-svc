package cl.dsoto.webservice.impl;

import cl.dsoto.webservice.resources.RegistrationStatusResource;
import cl.dsoto.webservice.resources.OnboardingTrainStep;
import cl.dsoto.webservice.resources.OnboardingTrainResource;
import cl.dsoto.services.OnboardingTrainService;
import cl.dsoto.webservice.OnboardingWebService;
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
public class DefaultOnboardingWebService implements OnboardingWebService {

    @Inject
    OnboardingTrainService trainService;

    @GET
    @Path("public/train")
    @PermitAll
    @Override
    public Response getPublicTrain(@QueryParam("stage") String stage) {
        return Response.ok(trainService.getPublicTrain(stage)).build();
    }

    @GET
    @Path("public/{registrationId}/status")
    @PermitAll
    @Override
    public Response getPublicRegistrationStatus(@PathParam("registrationId") String registrationId) {
        return trainService.getTrainByRegistrationId(registrationId)
                .map(this::publicRegistrationStatus)
                .map(status -> Response.ok(status).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("me/train")
    @RolesAllowed({"USER", "ADMIN"})
    @Override
    public Response getMyTrain(@Context SecurityContext securityContext) {
        if (securityContext == null || securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return trainService.getTrain(securityContext.getUserPrincipal().getName())
                .map(view -> Response.ok(view).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("{username}/train")
    @RolesAllowed("ADMIN")
    @Override
    public Response getUserTrain(@PathParam("username") String username) {
        return trainService.getTrain(username)
                .map(view -> Response.ok(view).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }

    private RegistrationStatusResource publicRegistrationStatus(OnboardingTrainResource train) {
        boolean confirmed = train.currentStep() != OnboardingTrainStep.REGISTRATION;
        return new RegistrationStatusResource(confirmed, train);
    }
}
