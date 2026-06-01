package cl.dsoto.events;

import cl.dsoto.services.EmailService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailConfirmationRequestedHandler {

    @Inject
    EmailService emailService;

    public void onEmailConfirmationRequested(@ObservesAsync EmailConfirmationRequested event) {
        try {
            emailService.sendEmailConfirmation(event.email(), event.confirmationUrl());
        } catch (Exception e) {
            Log.error("Email confirmation could not be sent to " + event.email(), e);
        }
    }
}
