package cl.dsoto.events;

import java.time.Instant;

public record EmailConfirmationRequested(
        String eventId,
        String eventType,
        int version,
        Instant occurredAt,
        String userId,
        String email,
        String confirmationUrl,
        Instant tokenExpiresAt
) {

    public static final String TYPE = "EmailConfirmationRequested";
    public static final int VERSION = 1;
}
