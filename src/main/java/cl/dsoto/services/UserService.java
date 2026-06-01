package cl.dsoto.services;


import cl.dsoto.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Created by root on 13-10-22.
 */
public interface UserService {

    List<User> getAllUsers();

    User saveUser(User user);

    void confirmEmail(String token);

    void resendEmailConfirmation(String email);

    boolean isUserActive(String username);

    void deleteUser(String id);

    Optional<User> getUser(String id);
}
