package com.ej.subscript.domain.audit;

import reactor.core.publisher.Mono;

/**
 * Outbound port for recording audit events.
 * <p>
 * Implementations are responsible for ensuring that a failure persisting
 * an event does NOT break the main business flow — auditing is desirable
 * but should not bring down a login if Mongo is unreachable.
 */
public interface AuditLog {

    /**
     * Records the event. Completes silently after persisting.
     */
    Mono<Void> record(AuditEvent event);
}
