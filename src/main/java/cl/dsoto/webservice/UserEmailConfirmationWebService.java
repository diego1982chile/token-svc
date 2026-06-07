package cl.dsoto.webservice;

import cl.dsoto.model.ResendConfirmationRequest;
import jakarta.ws.rs.core.Response;

public interface UserEmailConfirmationWebService {

    Response confirmEmail(String token);

    Response resendConfirmation(ResendConfirmationRequest request);
}
