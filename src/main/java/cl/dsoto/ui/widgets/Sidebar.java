package cl.dsoto.ui.widgets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class Sidebar extends Section implements HasEnabled, HasTheme {

    private Header header;
    private H2 title;
    private Span description;

    private VerticalLayout content;

    private Footer footer;
    private Button save;
    private Button cancel;

    public Sidebar(String title, Component... components) {
        this(title, null, components);
    }

    public Sidebar(String title, String description, Component... components) {
        addClassNames(LumoUtility.Background.BASE, LumoUtility.BoxShadow.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Overflow.HIDDEN,
                LumoUtility.Position.FIXED, "bottom-0", "top-0", "transition-all", "z-10");
        setMaxWidth(100, Unit.PERCENTAGE);
        setWidth(600, Unit.PIXELS);

        createHeader(title, description);
        createContent(components);
        createFooter();

        close();
    }

    private void createHeader(String title, String description) {
        this.title = new H2(title);
        this.title.addClassNames(LumoUtility.FontSize.XLARGE);

        VerticalLayout layout = new VerticalLayout(this.title);

        if (description != null) {
            this.description = new Span(description);
            this.description.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
            layout.add(this.description);
        }

        Button close = new Button(String.valueOf(VaadinIcon.CLOSE), e -> close());
        close.addClassNames(LumoUtility.Margin.Vertical.NONE);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        close.setAriaLabel("Close sidebar");
        close.setTooltipText("Close sidebar");

        this.header = new Header(layout, close);
        this.header.addClassNames(LumoUtility.Border.BOTTOM, LumoUtility.Display.FLEX, LumoUtility.JustifyContent.BETWEEN,
                LumoUtility.Padding.End.MEDIUM, LumoUtility.Padding.Start.LARGE, LumoUtility.Padding.Vertical.MEDIUM);

        if (description == null) {
            this.header.addClassNames(LumoUtility.AlignItems.CENTER);
        }

        add(this.header);
    }

    private void createContent(Component... components) {
        this.content = new VerticalLayout(components);
        this.content.addClassNames(LumoUtility.Flex.GROW, LumoUtility.Padding.Bottom.MEDIUM, LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Top.SMALL);
        //this.content.setFlexDirection(Layout.FlexDirection.COLUMN);
        add(this.content);
    }

    private void createFooter() {
        this.save = new Button("Save");
        this.save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        this.cancel = new Button("Cancel");

        this.footer = new Footer(this.cancel, this.save);
        this.footer.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL, LumoUtility.JustifyContent.END,
                LumoUtility.Padding.Horizontal.MEDIUM, LumoUtility.Padding.Vertical.SMALL);
        add(this.footer);
    }

    // TODO: Refocus the component that opened the sidebar after closing
// Abre el sidebar desplazándose desde la derecha
    public void open() {
        getElement().getStyle().set("right", "0"); // Mueve el sidebar a la vista
        getElement().getStyle().set("transition", "right 0.3s ease-in-out"); // Animación suave
        setEnabled(true);
    }

    // Cierra el sidebar desplazándose hacia afuera
    public void close() {
        getElement().getStyle().set("right", "-100%"); // Mueve el sidebar fuera de la vista
        getElement().getStyle().set("transition", "right 0.3s ease-in-out"); // Animación suave
        setEnabled(false);
    }

    public void addHeaderThemeName(String theme) {
        this.header.getElement().getThemeList().add(theme);
    }

    public void removeHeaderThemeName(String theme) {
        this.header.getElement().getThemeList().remove(theme);
    }

    public VerticalLayout getContent() {
        return this.content;
    }

    public Footer getFooter() {
        return this.footer;
    }
}
