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
 * Mongo representation of the domain {@code AuditEvent}.
 * Persisted to a collection named {@code audit_events}.
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
