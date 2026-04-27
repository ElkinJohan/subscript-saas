package com.ej.subscript.infrastructure.adapter.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Representación Mongo del {@code AuditEvent} de dominio.
 * Usar una colección llamada {@code audit_events}.
 */
@Document(collection = "audit_events")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditEventDocument {

    @Id
    private String id;
    private String type;
    private UUID ownerId;
    private Map<String, Object> data;
    private Instant occurredAt;
}
