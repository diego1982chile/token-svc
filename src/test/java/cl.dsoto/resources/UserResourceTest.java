package cl.dsoto.resources;

import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.repositories.UserRepository;
import cl.dsoto.services.ConfigService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.authentication.FormAuthConfig;
import jakarta.inject.Inject;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class UserResourceTest {

    @Inject
    private UserRepository userRepository;

    @Inject
    private RoleRepository roleRepository;

    @InjectMock
    private ConfigService configService;

    @BeforeEach
    public void init() throws NoSuchAlgorithmException, IOException {

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

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        Mockito.when(configService.getPrivateKey()).thenReturn(pair.getPrivate());
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
    public void shouldNotAccessAdminWhenAnonymous() {
        get("/api/users")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void shouldNotAccessUserWhenAdminAuthenticated() {
        given()
                //.auth().preemptive().basic("admin", "admin")
                .auth().form("user", "user", new FormAuthConfig("/api/auth/login", "j_username", "j_password"))
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void shouldAccessUserAndGetIdentityWhenUserAuthenticated() {
        given()
                //.auth().preemptive().basic("admin", "admin")
                .auth().form("admin", "admin", new FormAuthConfig("/api/auth/login", "j_username", "j_password"))
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("admin"));
    }
}

