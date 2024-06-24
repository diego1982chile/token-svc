package cl.dsoto.config;

import com.vaadin.flow.server.VaadinServlet;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/*", name = "VaadinServlet", asyncSupported = true, loadOnStartup = 1)
public class VaadinServletConfiguration extends VaadinServlet {
    // Configuración adicional si es necesaria
}
