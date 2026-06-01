package cl.dsoto.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class CdiDomainEventPublisher implements DomainEventPublisher {

    @Inject
    Event<EmailConfirmationRequested> emailConfirmationRequested;

    @Override
    public void publish(EmailConfirmationRequested event) {
        emailConfirmationRequested.fireAsync(event);
    }
}
