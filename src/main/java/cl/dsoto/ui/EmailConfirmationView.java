package cl.dsoto.ui;

import cl.dsoto.services.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.inject.Inject;

import java.util.List;

@Route("users/confirm-email")
@PageTitle("Confirm Email")
@AnonymousAllowed
public class EmailConfirmationView extends VerticalLayout implements BeforeEnterObserver {

    @Inject
    UserService userService;

    private final H1 title = new H1();
    private final Paragraph message = new Paragraph();
    private final EmailField emailField = new EmailField("Correo");
    private final Button resendButton = new Button("Enviar nuevo enlace", VaadinIcon.ENVELOPE.create());
    private final Div resendForm = new Div();

    public EmailConfirmationView() {
        addClassName("email-confirmation-view");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("box-sizing", "border-box")
                .set("min-height", "100vh")
                .set("padding", "var(--lumo-space-l)");

        title.addClassNames("m-0");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("font-weight", "600")
                .set("line-height", "1.2");

        message.addClassNames("m-0", "text-secondary");
        message.getStyle().set("line-height", "1.5");

        emailField.setWidthFull();
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Ingresa un correo valido");

        resendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        resendButton.addClickListener(event -> resendConfirmation());

        resendForm.add(emailField, resendButton);
        resendForm.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)");

        Div panel = new Div(title, message, resendForm);
        panel.addClassNames("alta-panel");
        panel.getStyle()
                .set("box-sizing", "border-box")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "var(--lumo-space-m)")
                .set("max-width", "460px")
                .set("width", "min(100%, 460px)");

        add(panel);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String token = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("token", List.of(""))
                .stream()
                .findFirst()
                .orElse("");

        if (token.isBlank()) {
            showInvalidTokenState();
            return;
        }

        try {
            userService.confirmEmail(token);
            showConfirmedState();
        } catch (IllegalArgumentException e) {
            showInvalidTokenState();
        } catch (Exception e) {
            showErrorState();
        }
    }

    private void showConfirmedState() {
        title.setText("Correo confirmado");
        message.setText("Tu identidad quedo activa. Ya puedes continuar el flujo desde la aplicacion o servicio donde iniciaste el registro.");
        resendForm.setVisible(false);
    }

    private void showInvalidTokenState() {
        title.setText("Enlace vencido o invalido");
        message.setText("Solicita un nuevo enlace de confirmacion para activar tu cuenta.");
        resendForm.setVisible(true);
    }

    private void showErrorState() {
        title.setText("No pudimos confirmar el correo");
        message.setText("Intenta nuevamente o solicita un nuevo enlace de confirmacion.");
        resendForm.setVisible(true);
    }

    private void resendConfirmation() {
        if (emailField.isEmpty() || emailField.isInvalid()) {
            emailField.setInvalid(true);
            return;
        }

        resendButton.setEnabled(false);
        try {
            userService.resendEmailConfirmation(emailField.getValue());
            showNotification(
                    "Si el correo existe y requiere confirmacion, enviaremos un nuevo enlace.",
                    NotificationVariant.LUMO_SUCCESS
            );
            emailField.clear();
        } catch (Exception e) {
            showNotification("No pudimos procesar la solicitud.", NotificationVariant.LUMO_ERROR);
        } finally {
            resendButton.setEnabled(true);
        }
    }

    private void showNotification(String text, NotificationVariant variant) {
        Notification notification = Notification.show(text, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
    }
}
