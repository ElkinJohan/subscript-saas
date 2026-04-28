package com.ej.subscript.domain.audit;

import reactor.core.publisher.Mono;

/**
 * Puerto de salida para el registro de eventos de auditoría.
 * <p>
 * La implementación es responsable de garantizar que un fallo persistiendo
 * un evento NO interrumpa el flujo de negocio principal — la auditoría es
 * deseable, pero no debería tirar abajo un login si Mongo está caído.
 */
public interface AuditLog {

    /**
     * Registra el evento. Completa silenciosamente al persistir.
     */
    Mono<Void> record(AuditEvent event);
}
