package cl.dsoto.ui;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinServletResponse;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver,
        ComponentEventListener<AbstractLogin.LoginEvent> {

    private static final String LOGIN_SUCCESS_URL = "/";

    private LoginForm login = new LoginForm();

    @Inject
    SecurityIdentity identity;

    public LoginView() {
        addClassName("login-view");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        login.addLoginListener(this);

        add(new H1("Token Admin"), login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }

        if (identity != null && !identity.isAnonymous()) {
            // Usuario ya está autenticado, pero volvió al login (por ejemplo con back)
            VaadinSession.getCurrent().getSession().invalidate();
            beforeEnterEvent.rerouteTo("login"); // recarga limpia
        }
    }

    @Override
    public void onComponentEvent(AbstractLogin.LoginEvent loginEvent) {
        try {
            HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();

            if (request.getUserPrincipal() != null) {
                Log.warn("User already logged-in");
                request.logout();
            }

            request.login(loginEvent.getUsername(), loginEvent.getPassword());


            getUI().ifPresent(ui -> ui.navigate("home"));
        } catch (Exception e) {
            Notification.show("Invalid credentials");
        }
    }
}
