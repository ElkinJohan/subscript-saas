package com.ej.subscript.domain.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Audit event capturing a significant action in the system.
 * <p>
 * Events are stored in MongoDB (not Postgres) for two reasons: high volume
 * and free-form shape — the {@code data} field varies by event type and a
 * relational schema would be expensive to evolve.
 *
 * @param ownerId id of the involved Owner, or {@code null} when the event
 *                happens without a session (e.g. failed login).
 * @param data    additional event payload (attempted email, IP, etc.).
 *                Free-form by design.
 */
public record AuditEvent(
        String id,
        AuditEventType type,
        UUID ownerId,
        Map<String, Object> data,
        Instant occurredAt
) {

    /**
     * Creates a new event with a random id and the current timestamp.
     */
    public static AuditEvent of(AuditEventType type, UUID ownerId, Map<String, Object> data) {
        return new AuditEvent(
                UUID.randomUUID().toString(),
                type,
                ownerId,
                data == null ? Map.of() : Map.copyOf(data),
                Instant.now()
        );
    }
}
