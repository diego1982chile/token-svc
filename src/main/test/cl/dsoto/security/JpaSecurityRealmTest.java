package cl.dsoto.security;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.repositories.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import jakarta.inject.Inject;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

import java.util.List;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class JpaSecurityRealmTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private RoleRepository roleRepository;

    @BeforeEach
    void init() {
        Role adminRole = Role.builder().rolename("ADMIN").build();
        Role userRole = Role.builder().rolename("USER").build();

        User user = new User();
        user.setUsername("user");
        user.setPassword(BcryptUtil.bcryptHash("user"));
        user.setRoles(List.of(userRole));

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(BcryptUtil.bcryptHash("admin"));
        admin.setRoles(List.of(adminRole, userRole));

        // reset and load all test users
        userRepository.save(user);
        userRepository.save(admin);
    }

    /*
    @Test
    void shouldAccessPublicWhenAnonymous() {
        get("/api/public")
                .then()
                .statusCode(HttpStatus.SC_OK);

    }
    */

    @Test
    void shouldNotAccessAdminWhenAnonymous() {
        get("/users")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);

    }

    @Test
    void
    shouldNotAccessUserWhenAdminAuthenticated() {
        given()
                .auth().preemptive().basic("user", "user")
                .when()
                .get("/users/me")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    void shouldAccessUserAndGetIdentityWhenUserAuthenticated() {
        given()
                .auth().preemptive().basic("admin", "admin")
                .when()
                .get("/users/me")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("admin"));
    }
}

