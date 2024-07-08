package cl.dsoto.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.apache.commons.configuration2.beanutils.BeanFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Date;

@Route("main")
@RolesAllowed({"ADMIN"})
public class MainView extends AppLayout { // implements BeforeEnterObserver {

    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    String cookieName;

    @Inject
    UsersView usersView;

    public MainView() {
        DrawerToggle toggle = new DrawerToggle();

        H1 logo = new H1("Token-SVC");
        logo.addClassNames("text-l", "m-m");
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

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logout);

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidth("100%");
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);

        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Users", "users", usersView));

        nav.addItem(new SideNavItem("Roles"));

        setPrimarySection(Section.DRAWER);

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);

        setPrimarySection(Section.NAVBAR);
        //addToNavbar(toggle);
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
