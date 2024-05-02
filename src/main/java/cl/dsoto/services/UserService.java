package cl.dsoto.services;


import cl.dsoto.entities.User;
import cl.dsoto.repositories.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
public class UserService {

    @Inject
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAllOrderByName();
        users.forEach(user -> user.setPassword(null));
        return users;
    }

    @Transactional
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
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void clear() {
        userRepository.deleteAll();
    }
}
