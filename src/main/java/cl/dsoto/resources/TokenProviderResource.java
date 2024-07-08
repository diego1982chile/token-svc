package cl.dsoto.resources;


import cl.dsoto.entities.Role;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.services.ConfigService;
import cl.dsoto.services.impl.DefaultCypherService;
import io.quarkus.logging.Log;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
import java.security.Principal;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;


/**
 * Created by root on 09-12-22.
 */
@ApplicationScoped
@Path("/auth")
public class TokenProviderResource {


    @Inject
    private RoleRepository roleRepository;

    private PrivateKey key;

    private Role ADMIN, USER;

    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    String cookieName;

    @Inject
    CurrentIdentityAssociation identity;

    @Inject
    private ConfigService configService;

    @PostConstruct
    public void init() {
        try {
            // Instantiate Spring Data factory
            //RepositoryFactorySupport factory = new JpaRepositoryFactory(entityManager);

            key = configService.getPrivateKey();

            //this.roleRepository = factory.getRepository(RoleRepository.class);
            List<Role> roles = roleRepository.findAll();
            ADMIN = roles.stream().filter(e -> e.getRolename().equals("ADMIN")).findFirst().orElseThrow();
            USER = roles.stream().filter(e -> e.getRolename().equals("USER")).findFirst().orElseThrow();
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login")
    public Response login(@FormParam("j_username") String username, @FormParam("j_password") String password,
                          @Context SecurityContext securityContext,
                          @Context HttpServletRequest request) {

        Principal userPrincipal = securityContext.getUserPrincipal();

        List<String> target = new ArrayList<>();

        //request.getSession(true); // Creates a new HTTP Session BEFORE the login.

        try {
            if (request.getUserPrincipal() != null) {
                Log.warn("User already logged-in");
                request.logout();
            }

            request.login(username, password);

            if (request.isUserInRole(ADMIN.getRolename()))
                target.add(ADMIN.getRolename());

            if (request.isUserInRole(USER.getRolename()))
                target.add(USER.getRolename());

        } catch (ServletException ex) {
            Log.error(ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String token = DefaultCypherService.generateJWT(key, username, target);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("jsessionid", request.getSession(false).getId());

        Cookie cookie = new Cookie("JSESSIONID", "value");
        cookie.setHttpOnly(true);
        //cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);

        String cookieString = String.format("%s=%s; Path=%s; HttpOnly; Secure; SameSite=None",
                cookie.getName(), cookie.getValue(), cookie.getPath());

        return Response.status(Response.Status.OK)
                .header(AUTHORIZATION, "Bearer ".concat(token))
                .header("Set-Cookie", cookieString)
                .cookie()
                .entity(response)
                .build();

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("logout")
    public Response logout(@Context HttpServletRequest request) {

        try {
            //request.getSession().invalidate();
            request.logout();
            //request.login("user", "logout");

            if (identity.getIdentity().isAnonymous()) {
                throw new UnauthorizedException("Not authenticated");
            }
            final NewCookie removeCookie = new NewCookie.Builder(cookieName)
                    .maxAge(0)
                    .expiry(Date.from(Instant.EPOCH))
                    .path("/")
                    .build();
            return Response.noContent().cookie(removeCookie).build();

        } catch (ServletException ex) {
            if (ex.getCause() instanceof AuthenticationFailedException) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Log.error(ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


}
