package cl.dsoto.resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("/auth-check")
public class AuthcheckResource {

    @GET
    @RolesAllowed("ADMIN") // O cualquier rol requerido
    public Response checkAuth(@Context HttpServletRequest request, @Context HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            // No hay sesión, borrar cookie y devolver 401
            clearCookieSesion(response);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Inspeccionar último acceso para validar si la sesión está "activa"
        long lastAccessed = session.getLastAccessedTime();
        long now = System.currentTimeMillis();

        // Definir el timeout que quieres (ejemplo 10 min)
        long timeout = 5 * 60 * 1000; // 10 minutos en ms

        System.out.println("now - lastAccessed = " + (now - lastAccessed));

        if (now - lastAccessed > timeout) {
            // Sesión inactiva > timeout
            session.invalidate();
            clearCookieSesion(response);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // Sesión válida
        return Response.ok().build();
    }

    private void clearCookieSesion(HttpServletResponse response) {
        Cookie cookie = new Cookie("MYSESSIONID", "");
        cookie.setPath("/token-service");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}
