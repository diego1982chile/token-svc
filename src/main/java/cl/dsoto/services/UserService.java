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
import java.util.Optional;

/**
 * Created by root on 13-10-22.
 */
public interface UserService {

    public List<User> getAllUsers();

    public User saveUser(User user);

    public void deleteUser(String id);

    public void clear();

    public Optional<User> getUser(String id);
}
