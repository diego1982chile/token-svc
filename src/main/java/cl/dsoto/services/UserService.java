package cl.dsoto.services;


import cl.dsoto.model.User;
import cl.dsoto.model.RegistrationResponse;

import java.util.List;
import java.util.Optional;

/**
 * Created by root on 13-10-22.
 */
public interface UserService {

    List<User> getAllUsers();

    User saveUser(User user);

    RegistrationResponse registerUser(String email, String password);

    User confirmEmail(String token);

    void resendEmailConfirmation(String email);

    boolean isUserActive(String username);

    void deleteUser(String id);

    Optional<User> getUser(String id);
}
