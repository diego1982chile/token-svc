package cl.dsoto.resources;


import cl.dsoto.model.Role;
import cl.dsoto.services.RoleService;
import cl.dsoto.services.ConfigService;
import cl.dsoto.services.CypherService;
import cl.dsoto.services.UserService;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;


/**
 * Created by root on 09-12-22.
 */
@ApplicationScoped
@Path("/auth")
public class TokenProviderResource {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";
    private static final String ONBOARDING_SERVICE_CLIENT_ID = "onboarding-svc";
    private static final long ACCESS_TOKEN_TTL_SECONDS = 3600L;


    @Inject
    private RoleService roleService;

    @Inject
    private UserService userService;

    private PrivateKey key;

    private Role admin, userRole;

    @Inject
    SecurityIdentity identity;

    @Inject
    private ConfigService configService;

    @Inject
    private CypherService cypherService;

    @ConfigProperty(name = "token.issuer")
    String jwtIssuer;

    @ConfigProperty(name = "token.access-audiences")
    List<String> jwtAudiences;

    @ConfigProperty(name = "token.service-client.audiences")
    List<String> serviceClientAudiences;

    @ConfigProperty(name = "token.service-client.onboarding-svc.secret")
    String onboardingServiceClientSecret;

    @ConfigProperty(name = "token.service-client.onboarding-svc.scopes")
    List<String> onboardingServiceClientScopes;

    @ConfigProperty(name = "quarkus.http.root-path")
    String rootPath;


    @PostConstruct
    public void init() {
        try {
            // Instantiate Spring Data factory
            //RepositoryFactorySupport factory = new JpaRepositoryFactory(entityManager);

            key = configService.getPrivateKey();

            //this.roleRepository = factory.getRepository(RoleRepository.class);
            List<Role> roles = new ArrayList<>(roleService.getAllRoles());
            admin = roles.stream().filter(e -> e.getRolename().equals(ADMIN_ROLE)).findFirst().orElseThrow();
            userRole = roles.stream().filter(e -> e.getRolename().equals(USER_ROLE)).findFirst().orElseThrow();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load JWT private key", e);
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login")
    public Response login(@FormParam("j_username") String username, @FormParam("j_password") String password,
                          @Context SecurityContext securityContext,
                          @Context HttpServletRequest request) {

        List<String> target = new ArrayList<>();

        //request.getSession(true); // Creates a new HTTP Session BEFORE the login.

        try {
            if (request.getUserPrincipal() != null) {
                Log.warn("User already logged-in");
                request.logout();
            }

            request.login(username, password);

            if (!userService.isUserActive(username)) {
                request.logout();
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            if (request.isUserInRole(admin.getRolename()))
                target.add(admin.getRolename());

            if (request.isUserInRole(userRole.getRolename()))
                target.add(userRole.getRolename());

        } catch (ServletException ex) {
            Log.error(ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String jwt = cypherService.generateJWT(key, username, target, jwtIssuer, jwtAudiences);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        HttpSession session = request.getSession(false);
        if (session != null) {
            response.put("jsessionid", session.getId());
        }

        return Response.status(Response.Status.OK)
                .header(AUTHORIZATION, "Bearer ".concat(jwt))
                .entity(response)
                .build();

    }

    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("client-credentials")
    public Response clientCredentials(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("scope") String scope
    ) {
        if (!ONBOARDING_SERVICE_CLIENT_ID.equals(clientId)
                || onboardingServiceClientSecret == null
                || onboardingServiceClientSecret.isBlank()
                || !onboardingServiceClientSecret.equals(clientSecret)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<String> requestedScopes = parseRequestedScopes(scope);
        if (requestedScopes.isEmpty() || !allowedScopes().containsAll(requestedScopes)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "invalid_scope"))
                    .build();
        }

        String jwt = cypherService.generateJWT(
                key,
                clientId,
                requestedScopes,
                jwtIssuer,
                serviceClientAudiences
        );

        return Response.status(Response.Status.OK)
                .entity(Map.of(
                        "access_token", jwt,
                        "token_type", "Bearer",
                        "expires_in", ACCESS_TOKEN_TTL_SECONDS
                ))
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("logout")
    public Response logout(@Context HttpServletRequest request) {

        try {
            request.logout();
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }

            NewCookie rootCookie = new NewCookie.Builder("MYSESSIONID")
                    .maxAge(0)
                    .expiry(Date.from(Instant.EPOCH))
                    .path("/")
                    .build();

            NewCookie appCookie = new NewCookie.Builder("MYSESSIONID")
                    .maxAge(0)
                    .expiry(Date.from(Instant.EPOCH))
                    .path(normalizeRootPath(rootPath))
                    .build();

            return Response.noContent().cookie(rootCookie, appCookie).build();

        } catch (ServletException ex) {
            Log.error(ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String normalizeRootPath(String value) {
        if (value == null || value.isBlank()) {
            return "/";
        }
        return value.startsWith("/") ? value : "/" + value;
    }

    private List<String> parseRequestedScopes(String scope) {
        if (scope == null || scope.isBlank()) {
            return List.of();
        }
        return Arrays.stream(scope.trim().split("\\s+"))
                .filter(value -> !value.isBlank())
                .toList();
    }

    private Set<String> allowedScopes() {
        return onboardingServiceClientScopes.stream().collect(Collectors.toSet());
    }


}
