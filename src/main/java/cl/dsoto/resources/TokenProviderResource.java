package cl.dsoto.resources;


import cl.dsoto.entities.Role;
import cl.dsoto.entities.User;
import cl.dsoto.repositories.RoleRepository;
import cl.dsoto.services.CypherService;
import io.quarkus.logging.Log;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.UnauthorizedException;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.ArrayList;
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
    CypherService cypherService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private RoleRepository roleRepository;

    private PrivateKey key;

    private Role ADMIN, USER;


    @PostConstruct
    public void init() {
        try {
            // Instantiate Spring Data factory
            //RepositoryFactorySupport factory = new JpaRepositoryFactory(entityManager);

            key = cypherService.readPrivateKey();

            //this.roleRepository = factory.getRepository(RoleRepository.class);
            List<Role> roles = roleRepository.findAll();
            ADMIN = roles.stream().filter(e -> e.getRolename().equals("ADMIN")).findAny().get();
            USER = roles.stream().filter(e -> e.getRolename().equals("USER")).findAny().get();
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login")
    //@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(User user, @Context HttpServletRequest request) {

        List<String> target = new ArrayList<>();

        try {
            if (request.getUserPrincipal() != null) {
                Log.warn("User already logged-in");
                request.logout();
            }

            request.login(user.getUsername(), user.getPassword());

            if (request.isUserInRole(ADMIN.getRolename()))
                target.add(ADMIN.getRolename());

            if (request.isUserInRole(USER.getRolename()))
                target.add(USER.getRolename());

        } catch (ServletException ex) {
            Log.error(ex.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String token = CypherService.generateJWT(key, user.getUsername(), target);

        Map<String, String> jwt = new HashMap<>();
        jwt.put("token", token);

        return Response.status(Response.Status.OK)
                .header(AUTHORIZATION, "Bearer ".concat(token))
                .entity(jwt)
                .build();

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("logout")
    public Response logout(@Context HttpServletRequest request) {

        try {
            //request.getSession().invalidate();
            request.logout();
            request.login("user", "logout");
        } catch (ServletException ex) {
            if (ex.getCause() instanceof AuthenticationFailedException) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            Log.error(ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

}
