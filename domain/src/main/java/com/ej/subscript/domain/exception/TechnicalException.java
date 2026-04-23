package com.ej.subscript.domain.exception;

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

    public String title() { return title; }
    public int status() { return status; }
    public String detail() { return detail; }
}
