package cl.dsoto.services.impl;


import cl.dsoto.entities.User;
import cl.dsoto.repositories.UserRepository;
import cl.dsoto.services.UserService;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
public class DefaultUserService implements UserService {

    @Inject
    private UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAllOrderByName();
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @Transactional
    @Override
    public User saveUser(User user) {

        User previous = userRepository.findByUsername(user.getUsername());

        if(previous != null) {
            if(user.getPassword() != null) {
                previous.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
            }
            previous.setRoles(user.getRoles());

            return userRepository.save(previous);
        }
        else {
            user.setPassword(BcryptUtil.bcryptHash(user.getPassword()));
            return userRepository.save(user);
        }
    }

    @Transactional
    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void clear() {
        userRepository.deleteAll();
    }

    @Override
    public Optional<User> getUser(String id) {
        return userRepository.findById(id);
    }
}
