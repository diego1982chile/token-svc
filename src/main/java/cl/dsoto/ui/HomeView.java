package cl.dsoto.ui;

import cl.dsoto.services.RoleService;
import cl.dsoto.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

@Route(value = "home", layout = MainView.class)
@RolesAllowed({"ADMIN"})
public class HomeView extends VerticalLayout {

    @Inject
    UserService userService;

    @Inject
    RoleService roleService;

    @PostConstruct
    void init() {
        configureView();

        add(createHeading(), createMetrics(), createActions());
    }

    private void configureView() {
        addClassNames("alta-page", "alta-home");
        setSizeFull();
        setPadding(true);
        setSpacing(false);
        getStyle().set("gap", "var(--lumo-space-l)");
    }

    private VerticalLayout createHeading() {

        H1 title = new H1("Administration");
        title.addClassNames("m-0");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "600");

        Paragraph subtitle = new Paragraph("Manage service users, roles, and access assignments.");
        subtitle.addClassNames("m-0", "text-secondary");

        VerticalLayout heading = new VerticalLayout(title, subtitle);
        heading.addClassName("alta-page-heading");
        heading.setPadding(false);
        heading.setSpacing(false);
        return heading;
    }

    private HorizontalLayout createMetrics() {
        HorizontalLayout metrics = new HorizontalLayout(
                metric("Users", String.valueOf(userService.getAllUsers().size()), "Registered principals"),
                metric("Roles", String.valueOf(roleService.getAllRoles().size()), "Available access groups")
        );
        metrics.addClassName("alta-card-row");
        metrics.setWidthFull();
        metrics.setSpacing(true);
        return metrics;
    }

    private HorizontalLayout createActions() {
        HorizontalLayout metrics = new HorizontalLayout(
                action("Manage users", "Review accounts and role assignments.", VaadinIcon.USERS, "users"),
                action("Manage roles", "Maintain reusable access groups.", VaadinIcon.BULLETS, "roles")
        );
        metrics.addClassName("alta-card-row");
        metrics.setWidthFull();
        metrics.setSpacing(true);
        return metrics;
    }

    private Div metric(String label, String value, String caption) {
        Span labelText = new Span(label);
        labelText.addClassNames("text-s", "text-secondary");

        Span valueText = new Span(value);
        valueText.getStyle()
                .set("display", "block")
                .set("font-size", "var(--lumo-font-size-xxxl)")
                .set("font-weight", "700")
                .set("line-height", "1");

        Span captionText = new Span(caption);
        captionText.addClassNames("text-s", "text-secondary");

        Div card = new Div(labelText, valueText, captionText);
        card.addClassNames("alta-card", "alta-metric-card");
        return card;
    }

    private Div action(String title, String description, VaadinIcon icon, String route) {
        Span titleText = new Span(title);
        titleText.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-m)");

        Paragraph descriptionText = new Paragraph(description);
        descriptionText.addClassNames("m-0", "text-s", "text-secondary");

        Button button = new Button("Open", icon.create());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        button.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(route)));

        VerticalLayout copy = new VerticalLayout(titleText, descriptionText);
        copy.setPadding(false);
        copy.setSpacing(false);

        HorizontalLayout content = new HorizontalLayout(copy, button);
        content.setWidthFull();
        content.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        content.expand(copy);

        Div card = new Div(content);
        card.addClassNames("alta-card", "alta-action-card");
        return card;
    }
}
