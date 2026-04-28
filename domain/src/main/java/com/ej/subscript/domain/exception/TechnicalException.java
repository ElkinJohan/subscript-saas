package com.ej.subscript.domain.exception;

/**
 * Excepción que representa una falla de infraestructura inesperada.
 * Siempre resulta en un HTTP 500. El {@code GlobalExceptionHandler}
 * la loguea como {@code ERROR} con stack trace completo, pero expone
 * al cliente solo un mensaje genérico (nunca detalles internos).
 *
 * <p>Ejemplos de uso: fallo de conexión a base de datos, timeout en
 * servicio externo, error de serialización no recuperable.
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
