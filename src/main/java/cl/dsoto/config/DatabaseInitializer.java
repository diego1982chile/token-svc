package cl.dsoto.config;


import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.repositories.UserRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by root on 09-12-22.
 */
@Startup
@Singleton
//@IfBuildProfile("dev")
public class DatabaseInitializer {

    @Inject
    private UserRepository userRepository;

    @Inject
    private RoleRepository roleRepository;


    @PostConstruct
    private void init() {
        initUsers();
        initRoles();
    }

    private void initUsers() {
        List<User> users = userRepository.findAll();
        List<Role> roles = roleRepository.findAll();

        Role adminRole = Role.builder().rolename("ADMIN").build();
        Role userRole = Role.builder().rolename("USER").build();

        if(users.isEmpty()) {

            String password = "123";

            // Protect user's password. The generated value can be stored in DB.
            password = BcryptUtil.bcryptHash(password);

            // Print out protected password
            System.out.println("My secure password = " + password);

            User admin = User.builder()
                    .username("diego.abelardo.soto@gmail.com")
                    .password(password)
                    .roles(Set.of(adminRole, userRole))
                    .build();

            userRepository.save(admin);
        }

    }

    private void initRoles() {
        List<Role> roles = roleRepository.findAll();

        Role adminRole = Role.builder().rolename("ADMIN").build();
        Role userRole = Role.builder().rolename("USER").build();

        if(roles.isEmpty()) {
            roleRepository.save(adminRole);
            roleRepository.save(userRole);
        }

    }

}
