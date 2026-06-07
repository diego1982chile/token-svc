package cl.dsoto.webservice;

import cl.dsoto.model.RegistrationRequest;
import cl.dsoto.model.ResendConfirmationRequest;
import cl.dsoto.model.User;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public interface UserWebService {

    String me(SecurityContext securityContext);

    Response getAllUsers();

    Response register(RegistrationRequest request);

    Response createUser(User user);

    Response deleteUser(String id);

    Response confirmEmail(String token);

    Response resendConfirmation(ResendConfirmationRequest request);
}
