package cl.dsoto.resources;

import cl.dsoto.entities.RoleEntity;
import cl.dsoto.entities.UserEntity;
import cl.dsoto.model.Role;
import cl.dsoto.model.UserStatus;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.repositories.UserRepository;
import cl.dsoto.services.ConfigService;
import cl.dsoto.services.CypherService;
import cl.dsoto.services.UserService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.authentication.FormAuthConfig;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;

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

        RoleEntity adminRole = getOrCreateRole("ADMIN");
        RoleEntity userRole = getOrCreateRole("USER");

        UserEntity user = new UserEntity();
        user.setUsername("user");
        user.setPassword(BcryptUtil.bcryptHash("user"));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(userRole));

        UserEntity admin = new UserEntity();
        admin.setUsername("admin");
        admin.setPassword(BcryptUtil.bcryptHash("admin"));
        admin.setStatus(UserStatus.ACTIVE);
        admin.setRoles(Set.of(adminRole, userRole));

        // reset and load all test users
        userRepository.save(user);
        userRepository.save(admin);

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        Mockito.when(configService.getPrivateKey()).thenReturn(pair.getPrivate());
    }

    private RoleEntity getOrCreateRole(String rolename) {
        RoleEntity role = roleRepository.findByRolename(rolename);
        if (role != null) {
            return role;
        }
        return roleRepository.save(RoleEntity.builder().rolename(rolename).build());
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
    public void shouldBuildLoginResponseWithoutFakeJSessionIdCookie() throws Exception {
        TokenProviderResource resource = new TokenProviderResource();
        CypherService cypherService = Mockito.mock(CypherService.class);
        UserService userService = Mockito.mock(UserService.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        Role adminRole = Role.builder().rolename("ADMIN").build();
        Role userRole = Role.builder().rolename("USER").build();

        setField(resource, "admin", adminRole);
        setField(resource, "userRole", userRole);
        setField(resource, "cypherService", cypherService);
        setField(resource, "userService", userService);
        setField(resource, "jwtIssuer", "https://apis.internal.dsoto.cl");
        setField(resource, "jwtAudience", "identity-svc");

        Mockito.when(userService.isUserActive("admin")).thenReturn(true);
        Mockito.when(request.getUserPrincipal()).thenReturn(null);
        Mockito.when(request.isUserInRole("ADMIN")).thenReturn(true);
        Mockito.when(request.isUserInRole("USER")).thenReturn(true);
        Mockito.when(request.getSession(false)).thenReturn(session);
        Mockito.when(session.getId()).thenReturn("session-id");
        Mockito.when(cypherService.generateJWT(any(), eq("admin"), anyList(), eq("https://apis.internal.dsoto.cl"), eq("identity-svc")))
                .thenReturn("jwt-token");

        Response response = resource.login("admin", "admin", securityContext, request);

        assertThat(response.getStatus(), is(HttpStatus.SC_OK));
        assertThat(response.getHeaderString(AUTHORIZATION), is("Bearer jwt-token"));
        assertThat(response.getHeaderString("Set-Cookie"), nullValue());
        assertThat(response.getEntity(), instanceOf(Map.class));
        Map<?, ?> responseBody = (Map<?, ?>) response.getEntity();
        assertThat(responseBody, hasEntry("jsessionid", "session-id"));
    }

    @Test
    public void shouldNotAccessAdminWhenAnonymous() {
        given()
                .redirects().follow(false)
                .when()
                .get("/api/users")
                .then()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    }

    @Test
    public void shouldNotAccessUserWhenAdminAuthenticated() {
        given()
                //.auth().preemptive().basic("admin", "admin")
                .auth().form("user", "user", new FormAuthConfig("/token-service/api/auth/login", "j_username", "j_password"))
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void shouldAccessUserAndGetIdentityWhenUserAuthenticated() {
        given()
                //.auth().preemptive().basic("admin", "admin")
                .auth().form("admin", "admin", new FormAuthConfig("/token-service/api/auth/login", "j_username", "j_password"))
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("admin"));
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
