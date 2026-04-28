package com.ej.subscript.domain.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Evento de auditoría que captura una acción significativa del sistema.
 * <p>
 * Los eventos se almacenan en MongoDB (no en Postgres) por dos razones:
 * volumen alto y forma libre — el campo {@code data} cambia según el tipo
 * y un schema relacional sería costoso de evolucionar.
 *
 * @param ownerId identificador del Owner involucrado, o {@code null} cuando
 *                el evento ocurre sin sesión (por ejemplo, login fallido).
 * @param data    información adicional del evento (email intentado, IP, etc.).
 *                Forma libre por diseño.
 */
public record AuditEvent(
        String id,
        AuditEventType type,
        UUID ownerId,
        Map<String, Object> data,
        Instant occurredAt
) {

    /**
     * Crea un evento nuevo con id aleatorio y timestamp actual.
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
