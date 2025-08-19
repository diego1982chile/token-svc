package cl.dsoto.ui;

import cl.dsoto.ui.widgets.Sidebar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "home", layout = MainView.class)
@RolesAllowed({"ADMIN"})
public class HomeView extends VerticalLayout { // implements BeforeEnterObserver {

    public HomeView() {
        add(new H1("Welcome to Token-Svc Admin"), new Paragraph("Token-Svc can be used to authorize access to resources in a decoupled and reusable manner within a micro-service architecture. This tool allows to manage principals and levels of access for each of the resources in the micro-service environment."));

        Sidebar sidebar = new Sidebar(
                "New event",
                "Fill in the blibber-blabber below to create a sensational event that will leave everyone flibber-gasted!",
                new Span("hola")
        );
        add(sidebar);

        Button button = new Button("Open sidebar", e -> {
            sidebar.open();
        });
        add(button);
    }

    /*
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        SecurityIdentity securityIdentity = (SecurityIdentity) VaadinService.getCurrentRequest().getWrappedSession().getAttribute("securityIdentity");

        if (!securityIdentity.getRoles().contains("ADMIN")) {
            event.rerouteTo(LoginView.class);
        }
    }
    */

}
