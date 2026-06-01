package cl.dsoto.config;


import cl.dsoto.entities.RoleEntity;
import cl.dsoto.entities.UserEntity;
import cl.dsoto.model.UserStatus;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.repositories.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

/**
 * Created by root on 09-12-22.
 */
@Startup
@Singleton
public class DatabaseInitializer {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final String DEV_ADMIN_USERNAME = "diego.abelardo.soto@gmail.com";

    @Inject
    private UserRepository userRepository;

    @Inject
    private RoleRepository roleRepository;

    @ConfigProperty(name = "identity.seed.enabled", defaultValue = "false")
    boolean seedEnabled;


    @PostConstruct
    void init() {
        if (!seedEnabled) {
            return;
        }
        initRoles();
        initUsers();
    }

    private void initUsers() {
        if(userRepository.count() == 0) {
            RoleEntity adminRole = roleRepository.findByRolename(ADMIN_ROLE);
            RoleEntity userRole = roleRepository.findByRolename(USER_ROLE);

            String password = "123";

            // Protect user's password. The generated value can be stored in DB.
            password = BcryptUtil.bcryptHash(password);

            UserEntity admin = UserEntity.builder()
                    .username(DEV_ADMIN_USERNAME)
                    .password(password)
                    .status(UserStatus.ACTIVE)
                    .roles(Set.of(adminRole, userRole))
                    .build();

            userRepository.save(admin);
        } else {
            ensureDevAdminHasAdminRole();
        }

    }

    private void ensureDevAdminHasAdminRole() {
        UserEntity admin = userRepository.findByUsername(DEV_ADMIN_USERNAME);
        RoleEntity adminRole = roleRepository.findByRolename(ADMIN_ROLE);

        if (admin == null || adminRole == null || admin.getRoles().contains(adminRole)) {
            return;
        }

        Set<RoleEntity> roles = admin.getRoles();
        roles.add(adminRole);
        admin.setRoles(roles);
        userRepository.save(admin);
    }

    private void initRoles() {
        if(roleRepository.findByRolename(ADMIN_ROLE) == null) {
            roleRepository.save(RoleEntity.builder().rolename(ADMIN_ROLE).build());
        }

        if(roleRepository.findByRolename(USER_ROLE) == null) {
            roleRepository.save(RoleEntity.builder().rolename(USER_ROLE).build());
        }

    }

}
