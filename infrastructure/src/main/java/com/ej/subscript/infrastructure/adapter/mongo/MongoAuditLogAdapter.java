package com.ej.subscript.infrastructure.adapter.mongo;

import com.ej.subscript.domain.audit.AuditEvent;
import com.ej.subscript.domain.audit.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Outbound adapter that persists {@link AuditEvent} into MongoDB.
 * <p>
 * If the write fails (Mongo unreachable, slow, etc.) the adapter logs a
 * warning and completes silently — auditing must not break the business
 * flow that invokes it. If stricter guarantees are required later, the
 * right move is a queue (Kafka/RabbitMQ) and accepting an
 * "audit-eventually-consistent" contract.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoAuditLogAdapter implements AuditLog {

    private final AuditEventMongoRepository repository;

    @Override
    public Mono<Void> record(AuditEvent event) {
        AuditEventDocument document = new AuditEventDocument(
                event.id(),
                event.type().name(),
                event.ownerId(),
                event.data(),
                event.occurredAt()
        );
        return repository.save(document)
                .doOnError(e -> log.warn("Audit write failed for type={}: {}", event.type(), e.getMessage()))
                .onErrorResume(e -> Mono.empty())
                .then();
    }
}
