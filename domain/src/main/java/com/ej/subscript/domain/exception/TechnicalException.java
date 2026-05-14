package com.ej.subscript.domain.exception;

/**
 * Represents an unexpected infrastructure failure. Always maps to HTTP 500.
 * The {@code GlobalExceptionHandler} logs it as {@code ERROR} with a full
 * stack trace but only exposes a generic message to the client (never
 * internal details).
 *
 * <p>Typical cases: database connection failure, external service timeout,
 * non-recoverable serialization error.
 */
public class TechnicalException extends DomainException {

    private final String title;
    private final int status;
    private final String detail;

    public TechnicalException(String title, int status, String detail) {
        super(detail);
        this.title = title;
        this.status = status;
        this.detail = detail;
    }

    public TechnicalException(String title, int status, String detail, Throwable cause) {
        super(detail);
        this.title = title;
        this.status = status;
        this.detail = detail;
        initCause(cause);
    }

    public String title() {
        return title;
    }

    public int status() {
        return status;
    }

    public String detail() {
        return detail;
    }
}
