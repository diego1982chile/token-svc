package cl.dsoto.events;

public interface DomainEventPublisher {

    void publish(EmailConfirmationRequested event);
}
