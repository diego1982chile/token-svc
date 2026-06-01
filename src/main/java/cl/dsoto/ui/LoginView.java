package cl.dsoto.ui;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.quarkus.logging.Log;
import cl.dsoto.security.SessionPolicy;
import cl.dsoto.services.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver,
        ComponentEventListener<AbstractLogin.LoginEvent> {

    private static final String LOGIN_SUCCESS_URL = "home";
    private static final String INVALID_CREDENTIALS_TITLE = "Invalid credentials";
    private static final String INVALID_CREDENTIALS_MESSAGE = "Check your username and password.";

    private LoginForm login = new LoginForm();
    private final Button themeToggle;
    private boolean darkMode;

    @Inject
    SecurityIdentity identity;

    @Inject
    UserService userService;

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        getStyle()
                .set("align-items", "center")
                .set("box-sizing", "border-box")
                .set("display", "grid")
                .set("height", "100vh")
                .set("justify-content", "center")
                .set("min-height", "100vh")
                .set("padding", "var(--lumo-space-l)")
                .set("place-items", "center")
                .set("position", "relative");

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        themeToggle = createThemeToggle();
        themeToggle.addClassName("alta-login-theme-toggle");
        themeToggle.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--alta-surface-border)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)")
                .set("height", "34px")
                .set("margin", "0")
                .set("position", "absolute")
                .set("right", "var(--lumo-space-l)")
                .set("top", "var(--lumo-space-l)")
                .set("width", "34px")
                .set("z-index", "10");

        login.addLoginListener(this);
        login.setForgotPasswordButtonVisible(false);
        login.setI18n(createLoginI18n());

        H1 title = new H1("Token-SVC");
        title.addClassNames("m-0");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "600")
                .set("padding-right", "48px");

        Paragraph subtitle = new Paragraph("Administration console");
        subtitle.addClassNames("m-0", "text-secondary");

        Div panel = new Div(themeToggle, title, subtitle, login);
        panel.addClassNames("alta-panel", "alta-login-panel");
        panel.getStyle()
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)")
                .set("max-width", "420px")
                .set("position", "relative")
                .set("width", "min(100%, 420px)");

        add(panel);

        addAttachListener(event -> event.getUI().getPage()
                .executeJs("return window.IdentityTheme && window.IdentityTheme.isDark();")
                .then(Boolean.class, storedDarkMode -> {
                    darkMode = Boolean.TRUE.equals(storedDarkMode);
                    applyTheme(event.getUI());
                    updateThemeToggle();
                }));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.showErrorMessage(INVALID_CREDENTIALS_TITLE, INVALID_CREDENTIALS_MESSAGE);
        }

        if (identity != null && !identity.isAnonymous()) {
            beforeEnterEvent.rerouteTo(LOGIN_SUCCESS_URL);
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

            if (!userService.isUserActive(loginEvent.getUsername())) {
                request.logout();
                login.showErrorMessage(INVALID_CREDENTIALS_TITLE, INVALID_CREDENTIALS_MESSAGE);
                return;
            }

            SessionPolicy.touch(request);
            getUI().ifPresent(ui -> ui.navigate(LOGIN_SUCCESS_URL));
        } catch (Exception e) {
            login.showErrorMessage(INVALID_CREDENTIALS_TITLE, INVALID_CREDENTIALS_MESSAGE);
        }
    }

    private LoginI18n createLoginI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        i18n.getForm().setTitle("Sign in");
        i18n.getForm().setUsername("Username");
        i18n.getForm().setPassword("Password");
        i18n.getForm().setSubmit("Sign in");
        i18n.getForm().setForgotPassword("");
        i18n.getErrorMessage().setTitle(INVALID_CREDENTIALS_TITLE);
        i18n.getErrorMessage().setMessage(INVALID_CREDENTIALS_MESSAGE);
        return i18n;
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
}
