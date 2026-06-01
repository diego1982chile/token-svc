package cl.dsoto.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

@Route("not-found")
@PageTitle("Not Found")
@AnonymousAllowed
public class NotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    public NotFoundView() {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle().set("background", "var(--lumo-contrast-5pct)");

        H1 title = new H1("404");
        title.addClassNames("m-0");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "600");

        Paragraph message = new Paragraph("The page you requested does not exist.");
        message.addClassNames("m-0", "text-secondary");

        Button home = new Button("Back to home", e -> getUI().ifPresent(ui -> ui.navigate("home")));
        home.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button login = new Button("Back to login", e -> getUI().ifPresent(ui -> ui.navigate("login")));

        Div actions = new Div(home, login);
        actions.getStyle()
                .set("display", "flex")
                .set("gap", "var(--lumo-space-s)")
                .set("flex-wrap", "wrap");

        Div panel = new Div(title, message, actions);
        panel.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "8px")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)")
                .set("max-width", "420px")
                .set("padding", "var(--lumo-space-xl)")
                .set("width", "100%");

        add(panel);
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
