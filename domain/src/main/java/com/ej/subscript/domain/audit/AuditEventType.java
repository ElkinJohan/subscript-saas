package com.ej.subscript.domain.audit;

/**
 * Tipos de eventos auditables emitidos por la aplicación.
 * Inmutable y exhaustivo — agregar tipos nuevos es una decisión consciente.
 */
public enum AuditEventType {
    AUTH_LOGIN_SUCCESS,
    AUTH_LOGIN_FAILED,
    AUTH_TOKEN_REFRESHED,
    AUTH_TOKEN_REUSE_DETECTED,
    AUTH_LOGOUT
}
