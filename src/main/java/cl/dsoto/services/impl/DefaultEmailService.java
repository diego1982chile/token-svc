package cl.dsoto.services.impl;

import cl.dsoto.services.EmailService;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class DefaultEmailService implements EmailService {

    private static final DateTimeFormatter TOKEN_EXPIRATION_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm z");

    @Inject
    Mailer mailer;

    @Inject
    @Location("emails/email-confirmation.html")
    Template emailConfirmationTemplate;

    @ConfigProperty(name = "email.confirmation.subject", defaultValue = "Confirma tu correo")
    String subject;

    @ConfigProperty(name = "app.name", defaultValue = "Token-SVC")
    String appName;

    @ConfigProperty(name = "email.confirmation.ttl-hours", defaultValue = "24")
    long tokenTtlHours;

    @ConfigProperty(name = "email.confirmation.time-zone", defaultValue = "America/Santiago")
    String tokenExpirationTimeZone;

    @Override
    public void sendEmailConfirmation(String email, String confirmationUrl) {
        String html = emailConfirmationTemplate
                .data("subject", subject)
                .data("appName", appName)
                .data("confirmationUrl", confirmationUrl)
                .data("tokenExpiresAt", formatTokenExpiresAt())
                .render();

        mailer.send(Mail.withHtml(email, subject, html));
    }

    private String formatTokenExpiresAt() {
        return ZonedDateTime.now(ZoneId.of(tokenExpirationTimeZone))
                .plusHours(tokenTtlHours)
                .format(TOKEN_EXPIRATION_FORMATTER);
    }
}
