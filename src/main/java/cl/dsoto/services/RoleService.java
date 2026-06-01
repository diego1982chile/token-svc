package cl.dsoto.services;


import cl.dsoto.model.Role;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Created by root on 13-10-22.
 */
public interface RoleService {

    Set<Role> getAllRoles();

    @Transactional
    Role saveRole(Role role);

    @Transactional
    Role updateRole(Role role);

    @Transactional
    void deleteRole(Long id);

    Optional<Role> getRole(Long id);
}
