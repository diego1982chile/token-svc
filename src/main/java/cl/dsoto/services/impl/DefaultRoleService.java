package cl.dsoto.services.impl;


import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.repositories.UserRepository;
import cl.dsoto.services.RoleService;
import io.quarkus.elytron.security.common.BcryptUtil;
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
@RequestScoped
public class DefaultRoleService implements RoleService {


    @Inject
    private RoleRepository roleRepository;

    @Override
    public Set<Role> getAllRoles() {
        return roleRepository.findAllOrderByName();
    }

    @Transactional
    @Override
    public Role saveRole(Role role) {

        Role previous = roleRepository.findByRolename(role.getPreviousRolename());

        if(previous != null) {
            previous.setRolename(role.getRolename());
            previous.setPreviousRolename(role.getPreviousRolename());
            return roleRepository.save(previous);
        }
        else {
            return roleRepository.save(role);
        }
    }

    @Override
    @Transactional
    public Role updateRole(Role role) {
        Role previous = roleRepository.findByRolename(role.getPreviousRolename());
        roleRepository.delete(previous);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleRepository.deleteById(id.toString());
    }

    @Override
    public Optional<Role> getRole(String id) {
        return roleRepository.findById(id);
    }
}
