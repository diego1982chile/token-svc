package cl.dsoto.services.impl;


import cl.dsoto.entities.RoleEntity;
import cl.dsoto.mappers.RoleMapper;
import cl.dsoto.model.Role;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.services.RoleService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Created by root on 13-10-22.
 */
@RequestScoped
public class DefaultRoleService implements RoleService {


    @Inject
    private RoleRepository roleRepository;

    @Inject
    private RoleMapper roleMapper;

    @Override
    @Transactional
    public Set<Role> getAllRoles() {
        return roleMapper.toModelSet(roleRepository.findAllOrderByName());
    }

    @Transactional
    @Override
    public Role saveRole(Role role) {

        RoleEntity previous = roleRepository.findByRolename(role.getPreviousRolename());

        if(previous != null) {
            previous.setRolename(role.getRolename());
            previous.setPreviousRolename(role.getPreviousRolename());
            return roleMapper.toModel(roleRepository.save(previous));
        }
        else {
            return roleMapper.toModel(roleRepository.save(roleMapper.toEntity(role)));
        }
    }

    @Override
    @Transactional
    public Role updateRole(Role role) {
        RoleEntity previous = roleRepository.findByRolename(role.getPreviousRolename());
        roleRepository.delete(previous);
        return roleMapper.toModel(roleRepository.save(roleMapper.toEntity(role)));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Optional<Role> getRole(Long id) {
        return roleRepository.findById(id).map(roleMapper::toModel);
    }
}
