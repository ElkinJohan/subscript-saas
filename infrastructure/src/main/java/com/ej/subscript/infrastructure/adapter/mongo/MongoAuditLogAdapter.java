package com.ej.subscript.infrastructure.adapter.mongo;

import com.ej.subscript.domain.audit.AuditEvent;
import com.ej.subscript.domain.audit.AuditLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida que persiste {@link AuditEvent} en MongoDB.
 * <p>
 * Si la escritura falla (Mongo caído, lento, etc.) registra un warning y
 * completa silenciosamente — la auditoría no debe romper el flujo de negocio
 * que la invoca. Si necesitamos garantías estrictas, deberíamos pasar a una
 * cola (Kafka/RabbitMQ) y aceptar audit-eventually-consistent como contrato.
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
