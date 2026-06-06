package cl.dsoto.entities;

import cl.dsoto.model.IdentityEventType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "IDENTITY_EVENT_LOG")
public class IdentityEventLogEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;

    @Enumerated(EnumType.STRING)
    private IdentityEventType eventType;

    private String subject;

    private String registrationId;

    private Instant occurredAt;

    private Instant createdAt;

    public static IdentityEventLogEntryEntity create(
            String eventId,
            IdentityEventType eventType,
            String subject,
            String registrationId,
            Instant occurredAt
    ) {
        return IdentityEventLogEntryEntity.builder()
                .eventId(eventId)
                .eventType(eventType)
                .subject(subject)
                .registrationId(registrationId)
                .occurredAt(occurredAt)
                .createdAt(Instant.now())
                .build();
    }
}
