package cl.dsoto.services;


import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.repositories.RoleRepository;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by root on 13-10-22.
 */
public interface RoleService {

    public Set<Role> getAllRoles();

    @Transactional
    public Role saveRole(Role role);

    @Transactional
    public Role updateRole(Role role);

    @Transactional
    public void deleteRole(Long id);

    public Optional<Role> getRole(String id);
}
