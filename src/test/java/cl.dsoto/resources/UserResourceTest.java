package cl.dsoto.resources;

import cl.dsoto.entities.RoleEntity;
import cl.dsoto.entities.UserEntity;
import cl.dsoto.model.IdentityEventType;
import cl.dsoto.model.Role;
import cl.dsoto.model.UserStatus;
import cl.dsoto.model.OnboardingState;
import cl.dsoto.entities.OnboardingProcess;
import cl.dsoto.repositories.IdentityEventLogEntryRepository;
import cl.dsoto.repositories.OnboardingProcessRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
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

    @Inject
    private OnboardingProcessRepository onboardingProcessRepository;

    @Inject
    private IdentityEventLogEntryRepository identityEventLogEntryRepository;

    @Inject
    private CypherService cypherService;

    @InjectMock
    private ConfigService configService;

    private KeyPair keyPair;

    @BeforeEach
    public void init() throws NoSuchAlgorithmException, IOException {
        identityEventLogEntryRepository.deleteAll();

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
        keyPair = generator.generateKeyPair();

        Mockito.when(configService.getPrivateKey()).thenReturn(keyPair.getPrivate());
        Mockito.when(configService.getPublicKey()).thenReturn(keyPair.getPublic());
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
        setField(resource, "jwtAudiences", List.of("identity-svc", "onboarding-svc"));

        Mockito.when(userService.isUserActive("admin")).thenReturn(true);
        Mockito.when(request.getUserPrincipal()).thenReturn(null);
        Mockito.when(request.isUserInRole("ADMIN")).thenReturn(true);
        Mockito.when(request.isUserInRole("USER")).thenReturn(true);
        Mockito.when(request.getSession(false)).thenReturn(session);
        Mockito.when(session.getId()).thenReturn("session-id");
        Mockito.when(cypherService.generateJWT(
                        any(),
                        eq("admin"),
                        anyList(),
                        eq("https://apis.internal.dsoto.cl"),
                        eq(List.of("identity-svc", "onboarding-svc"))))
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
    public void shouldExposePublicJwksWhenAnonymous() {
        given()
                .when()
                .get("/api/.well-known/jwks.json")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("keys[0].kty", is("RSA"))
                .body("keys[0].use", is("sig"))
                .body("keys[0].alg", is("RS256"))
                .body("keys[0].kid", is("apisKey"))
                .body("keys[0].n", notNullValue())
                .body("keys[0].e", notNullValue());
    }

    @Test
    public void shouldRegisterUserWhenAnonymous() {
        String email = "new.user@example.com";

        String registrationId = given()
                .contentType("application/json")
                .body(Map.of(
                        "email", email,
                        "password", "secret123"
                ))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("registrationId", notNullValue())
                .extract()
                .path("registrationId");

        UserEntity user = userRepository.findByUsername(email);

        assertThat(user.getStatus(), is(UserStatus.PENDING));
        assertThat(user.getRoles().stream().anyMatch(role -> "USER".equals(role.getRolename())), is(true));
        assertThat(onboardingProcessRepository.findById(email).orElseThrow().getRegistrationId(), is(registrationId));

        var identityEvents = identityEventLogEntryRepository.findAll();
        assertThat(identityEvents.size(), is(1));
        assertThat(identityEvents.get(0).getEventType(), is(IdentityEventType.USER_REGISTERED));
        assertThat(identityEvents.get(0).getSubject(), is(email));
        assertThat(identityEvents.get(0).getRegistrationId(), is(registrationId));
    }

    @Test
    public void shouldExposeIdentityEventFeedForAdmin() {
        String email = "feed.user@example.com";

        String registrationId = given()
                .contentType("application/json")
                .body(Map.of(
                        "email", email,
                        "password", "secret123"
                ))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .extract()
                .path("registrationId");

        given()
                .auth().form("admin", "admin", new FormAuthConfig("/token-service/api/auth/login", "j_username", "j_password"))
                .queryParam("after", 0)
                .queryParam("limit", 10)
                .when()
                .get("/api/internal/identity-events")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("items[0].eventType", is("USER_REGISTERED"))
                .body("items[0].subject", is(email))
                .body("items[0].registrationId", is(registrationId))
                .body("items[0].cursor", notNullValue())
                .body("nextCursor", notNullValue())
                .body("hasMore", is(false));
    }

    @Test
    public void shouldAppendEmailVerifiedIdentityEventWhenEmailIsConfirmed() {
        String email = "confirmed.event@example.com";
        String token = cypherService.generateEmailConfirmationJWT(
                keyPair.getPrivate(),
                email,
                "https://apis.internal.dsoto.cl",
                "identity-svc"
        );

        given()
                .contentType("application/json")
                .body(Map.of(
                        "email", email,
                        "password", "secret123"
                ))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.SC_ACCEPTED);

        given()
                .queryParam("token", token)
                .when()
                .get("/api/users/confirm-email")
                .then()
                .statusCode(HttpStatus.SC_OK);

        var identityEvents = identityEventLogEntryRepository.findAll();
        assertThat(identityEvents.size(), is(2));
        assertThat(identityEvents.stream()
                .anyMatch(event -> event.getEventType() == IdentityEventType.USER_REGISTERED
                        && email.equals(event.getSubject())), is(true));
        assertThat(identityEvents.stream()
                .anyMatch(event -> event.getEventType() == IdentityEventType.EMAIL_VERIFIED
                        && email.equals(event.getSubject())), is(true));
    }

    @Test
    public void shouldRejectInvalidAnonymousRegistration() {
        given()
                .contentType("application/json")
                .body(Map.of("email", "missing.password@example.com"))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void shouldReturnRegistrationIdForExistingActiveUser() {
        String email = "active.without.onboarding@example.com";
        RoleEntity userRole = getOrCreateRole("USER");
        UserEntity user = UserEntity.builder()
                .username(email)
                .password(BcryptUtil.bcryptHash("secret123"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        String registrationId = given()
                .contentType("application/json")
                .body(Map.of(
                        "email", email,
                        "password", "ignored123"
                ))
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(HttpStatus.SC_ACCEPTED)
                .body("registrationId", notNullValue())
                .extract()
                .path("registrationId");

        assertThat(registrationId, notNullValue());
    }

    @Test
    public void shouldDeleteOnboardingProcessWhenUserIsDeleted() {
        String email = "delete.with.onboarding@example.com";
        RoleEntity userRole = getOrCreateRole("USER");
        UserEntity user = UserEntity.builder()
                .username(email)
                .password(BcryptUtil.bcryptHash("secret123"))
                .status(UserStatus.ACTIVE)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);
        onboardingProcessRepository.save(OnboardingProcess.builder()
                .username(email)
                .currentState(OnboardingState.EMAIL_VERIFIED)
                .build());

        given()
                .auth().form("admin", "admin", new FormAuthConfig("/token-service/api/auth/login", "j_username", "j_password"))
                .when()
                .delete("/api/users/delete/" + email)
                .then()
                .statusCode(HttpStatus.SC_OK);

        assertThat(userRepository.findByUsername(email), nullValue());
        assertThat(onboardingProcessRepository.findById(email).isEmpty(), is(true));
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
