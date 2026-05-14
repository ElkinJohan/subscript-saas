package com.ej.subscript.domain.audit;

/**
 * Auditable event types emitted by the application.
 * Closed and exhaustive — adding a new type is a deliberate decision.
 */
public enum AuditEventType {
    AUTH_LOGIN_SUCCESS,
    AUTH_LOGIN_FAILED,
    AUTH_TOKEN_REFRESHED,
    AUTH_TOKEN_REUSE_DETECTED,
    AUTH_LOGOUT
}
