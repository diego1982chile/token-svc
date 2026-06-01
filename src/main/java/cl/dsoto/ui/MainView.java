package cl.dsoto.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.PollEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.quarkus.annotation.UIScoped;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import io.quarkus.security.identity.SecurityIdentity;
import cl.dsoto.security.SessionPolicy;

@Route("main")
@RolesAllowed({"ADMIN"})
@UIScoped
public class MainView extends AppLayout implements AfterNavigationObserver {

    @Inject
    SecurityIdentity securityIdentity;

    private final MenuItem userItem;
    private final Button themeToggle;
    private boolean darkMode;
    private Registration pollRegistration;

    public MainView() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Toggle navigation");
        toggle.addClassName("alta-shell-toggle");

        Image logo = new Image("images/token-svc-logo.svg", "IDENTITY-SVC");
        logo.addClassName("alta-shell-logo");

        SideNav nav = new SideNav();
        nav.addClassName("alta-shell-nav");
        nav.addItem(new SideNavItem("Home", "home", VaadinIcon.HOME.create()));
        nav.addItem(new SideNavItem("Users", "users", VaadinIcon.USER.create()));
        nav.addItem(new SideNavItem("Roles", "roles", VaadinIcon.BULLETS.create()));

        setPrimarySection(Section.DRAWER);

        Scroller scroller = new Scroller(nav);
        scroller.addClassNames(LumoUtility.Padding.SMALL, "alta-shell-drawer");

        addToDrawer(scroller);

        setPrimarySection(Section.NAVBAR);

        MenuBar userMenu = new MenuBar();
        userMenu.addClassName("alta-user-menu");
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userItem = userMenu.addItem("User");
        userItem.getElement().setAttribute("title", "User menu");
        userItem.getSubMenu().addItem("Log out", event -> performLogout());

        themeToggle = createThemeToggle();

        HorizontalLayout brand = new HorizontalLayout(toggle, logo);
        brand.addClassName("alta-shell-brand");
        brand.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        brand.setSpacing(false);

        HorizontalLayout topBar = new HorizontalLayout(brand, themeToggle, userMenu);
        topBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        topBar.expand(brand);
        topBar.setSpacing(false);
        topBar.setWidth("100%");
        topBar.addClassNames("py-s", "px-m");
        topBar.addClassName("alta-shell-topbar");
        addToNavbar(topBar);

        addAttachListener(event -> {
            event.getUI().getPage()
                    .executeJs("return window.IdentityTheme && window.IdentityTheme.isDark();")
                    .then(Boolean.class, storedDarkMode -> {
                        darkMode = Boolean.TRUE.equals(storedDarkMode);
                        applyTheme(event.getUI());
                        updateThemeToggle();
                    });
            if (pollRegistration == null) {
                event.getUI().setPollInterval(2000);
                pollRegistration = event.getUI().addPollListener(this::onPoll);
            }
        });

        addDetachListener(event -> {
            if (pollRegistration != null) {
                pollRegistration.remove();
                pollRegistration = null;
            }
        });
    }

    private Button createThemeToggle() {
        Button button = new Button();
        button.addClassName("alta-theme-toggle");
        button.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY);
        button.addClickListener(event -> {
            darkMode = !darkMode;
            button.getUI().ifPresent(ui -> {
                applyTheme(ui);
                updateThemeToggle();
            });
        });
        updateThemeToggle(button, false);
        return button;
    }

    private void applyTheme(UI ui) {
        ui.getElement().getThemeList().set("dark", darkMode);
        ui.getPage().executeJs(
                "window.IdentityTheme ? window.IdentityTheme.set($0) : " +
                        "(document.documentElement.setAttribute('theme', $0 ? 'dark' : ''), " +
                        "localStorage.setItem('identity-theme', $0 ? 'dark' : 'light'));",
                darkMode);
    }

    private void updateThemeToggle() {
        updateThemeToggle(themeToggle, darkMode);
    }

    private void updateThemeToggle(Button button, boolean darkMode) {
        button.setIcon(new Icon(darkMode ? VaadinIcon.SUN_O : VaadinIcon.MOON_O));
        button.getElement().setAttribute("title", darkMode ? "Use light mode" : "Use dark mode");
        button.getElement().setAttribute("aria-label", darkMode ? "Use light mode" : "Use dark mode");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        userItem.setText(getCurrentUserName());
    }

    private void performLogout() {
        try {
            UI ui = UI.getCurrent();
            HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
            request.logout();
            VaadinSession.getCurrent().getSession().invalidate();
            ui.getPage().executeJs("window.location.replace('login');");
        }
        catch (Exception ex) {
            Notification.show(ex.getMessage());
        }
    }

    private String getCurrentUserName() {
        if (securityIdentity != null && securityIdentity.getPrincipal() != null) {
            return securityIdentity.getPrincipal().getName();
        }
        return "Anonymous";
    }

    private void onPoll(PollEvent event) {
        HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
        if (SessionPolicy.isExpired(request.getSession(false))) {
            performLogout();
        }
    }

}
