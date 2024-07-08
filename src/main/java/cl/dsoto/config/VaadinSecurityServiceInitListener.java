package cl.dsoto.config;

import cl.dsoto.ui.AccessDeniedView;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class VaadinSecurityServiceInitListener { // implements VaadinServiceInitListener {

    @Inject
    AccessAnnotationChecker accessChecker;

    @Inject
    SecurityIdentity securityIdentity;

    public void serviceInit(@Observes ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInitEvent -> {
            uiInitEvent.getUI().addBeforeEnterListener(enterEvent -> {

                if (!accessChecker.hasAccess(enterEvent.getNavigationTarget())) {
                    enterEvent.rerouteTo(AccessDeniedView.class); // Redirect to access denied if user lacks permission
                }

            });
        });

        event.addIndexHtmlRequestListener(response -> {
            // IndexHtmlRequestListener to change the bootstrap page
        });

        event.addDependencyFilter((dependencies, filterContext) -> {
            // DependencyFilter to add/remove/change dependencies sent to
            // the client
            return dependencies;
        });

        event.addRequestHandler((session, request, response) -> {
            // RequestHandler to change how responses are handled
            return false;
        });
    }
}
