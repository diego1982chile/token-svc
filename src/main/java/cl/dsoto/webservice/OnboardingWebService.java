package cl.dsoto.webservice;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public interface OnboardingWebService {

    Response getPublicTrain(String stage);

    Response getPublicRegistrationStatus(String registrationId);

    Response getMyTrain(SecurityContext securityContext);

    Response getUserTrain(String username);
}
