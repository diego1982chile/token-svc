package cl.dsoto.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.NewCookie;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Instant;
import java.util.Date;

@Route("users")
@RolesAllowed({"ADMIN"})
public class UsersView extends Div { // implements BeforeEnterObserver {

    public UsersView() {
        setText("Users...");
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
