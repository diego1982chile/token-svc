package cl.dsoto.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import jakarta.servlet.http.HttpServletRequest;

@Route("access-denied")
@PageTitle("Access Denied")
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        addClassName("alta-page");

        H1 title = new H1("Access denied");
        title.addClassNames("m-0");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "600");

        Paragraph message = new Paragraph("You do not have permission to view this page.");
        message.addClassNames("m-0", "text-secondary");

        Button login = new Button("Back to login", e -> signOutAndGoToLogin());
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Div panel = new Div(title, message, login);
        panel.addClassNames("alta-panel", "alta-message-panel");

        add(panel);
    }

    private void signOutAndGoToLogin() {
        getUI().ifPresent(ui -> {
            try {
                HttpServletRequest request = VaadinServletRequest.getCurrent().getHttpServletRequest();
                request.logout();
                VaadinSession.getCurrent().getSession().invalidate();
            } catch (Exception ignored) {
                // The user may already be anonymous or the session may already be invalid.
            }
            ui.getPage().executeJs("window.location.replace('login');");
        });
    }
}
