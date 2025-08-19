package cl.dsoto.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.quarkus.annotation.UIScoped;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.NewCookie;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Date;

@Route("main")
@RolesAllowed({"ADMIN"})
@UIScoped
public class MainView extends AppLayout { // implements BeforeEnterObserver {

    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    String cookieName;

    @Inject
    UsersView usersView;

    @Inject
    RolesView rolesView;

    public MainView() {
        DrawerToggle toggle = new DrawerToggle();

        H1 logo = new H1("Token-SVC");
        logo.addClassNames("text-l", "m-m");
        logo.getStyle().set("font-size", "var(--lumo-font-size-l)")
                .set("left", "var(--lumo-space-l)").set("margin", "0");

        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Users", "users", VaadinIcon.USER.create()));

        nav.addItem(new SideNavItem("Roles", "roles", VaadinIcon.BULLETS.create()));

        setPrimarySection(Section.DRAWER);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);

        setPrimarySection(Section.NAVBAR);

        addToNavbar(toggle);

        // Crear el contenedor del Navbar
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setSpacing(true);

        Button logout = createLogout();

        HorizontalLayout header = new HorizontalLayout(logo, logout);

        header.getElement();

        addToNavbar(header);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");
    }



    private Button createLogout() {
        Button logout = new Button("Log out");

        logout.addClickListener(e -> {
            try {
                UI.getCurrent().getPage().setLocation("login");
                HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
                request.logout();

                new NewCookie.Builder(cookieName)
                        .maxAge(0)
                        .expiry(Date.from(Instant.EPOCH))
                        .path("/")
                        .build();
            }
            catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        return logout;
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
