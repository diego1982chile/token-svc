package cl.dsoto.config;

import cl.dsoto.ui.AccessDeniedView;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

@ApplicationScoped
public class VaadinSecurityServiceInitListener { // implements VaadinServiceInitListener {

    @Inject
    AccessAnnotationChecker accessChecker;

    @Inject
    Provider<SecurityIdentity> identityProvider;

    public void serviceInit(@Observes ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiInitEvent -> {
            uiInitEvent.getUI().addBeforeEnterListener(enterEvent -> {

                String path = enterEvent.getLocation().getPath();
                boolean publicRoute = path.endsWith("login")
                        || enterEvent.getLocation().getPath().endsWith("logout")
                        || enterEvent.getLocation().getPath().endsWith("access-denied")
                        || path.endsWith("users/confirm-email");

                if (!accessChecker.hasAccess(enterEvent.getNavigationTarget())) {
                    enterEvent.rerouteTo(AccessDeniedView.class); // Redirect to access denied if user lacks permission
                    return;
                }

                if (!publicRoute) {
                    SecurityIdentity currentIdentity = identityProvider.get();
                    if (currentIdentity == null || currentIdentity.isAnonymous()) {
                        enterEvent.rerouteTo(AccessDeniedView.class);
                    }
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
    }
}
