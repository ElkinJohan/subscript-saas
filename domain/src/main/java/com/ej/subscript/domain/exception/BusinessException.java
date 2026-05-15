package com.ej.subscript.domain.exception;

/**
 * Represents a known, expected business-rule violation. Always maps to an
 * HTTP 4xx status code (400, 401, 404, 409, 422...).
 *
 * <p>The {@code GlobalExceptionHandler} writes the {@code status} directly
 * to the HTTP response and exposes {@code title} + {@code detail} to the
 * client. It is not logged as an error — it represents a normal domain flow.
 *
 * @param title  short error name (e.g. "Plan not found")
 * @param status matching HTTP status code (e.g. 404)
 * @param detail descriptive message intended for the client
 */
public class BusinessException extends DomainException {

    private final String title;
    private final int status;
    private final String detail;

    public BusinessException(String title, int status, String detail) {
        super(detail);
        this.title = title;
        this.status = status;
        this.detail = detail;
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
