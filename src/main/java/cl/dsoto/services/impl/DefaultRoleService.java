package cl.dsoto.services.impl;


import cl.dsoto.entities.Role;
import cl.dsoto.repositories.RoleRepository;
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
public class DefaultRoleService {

    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAllOrderByName();
    }

    @Transactional
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(Role role) {
        Role previous = roleRepository.findByRolename(role.getPreviousRolename());
        roleRepository.delete(previous);
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(String id) {
        roleRepository.deleteById(id);
    }
}
