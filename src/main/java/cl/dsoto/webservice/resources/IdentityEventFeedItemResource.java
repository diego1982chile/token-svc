package cl.dsoto.webservice.resources;

import cl.dsoto.model.IdentityEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentityEventFeedItemResource {

    private Long cursor;
    private String eventId;
    private IdentityEventType eventType;
    private String subject;
    private String registrationId;
    private Instant occurredAt;
}
