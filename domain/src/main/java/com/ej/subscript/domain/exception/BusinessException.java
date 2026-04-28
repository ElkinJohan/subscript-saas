package com.ej.subscript.domain.exception;

/**
 * Excepción que representa una violación de regla de negocio conocida y esperada.
 * Siempre mapea a un código HTTP 4xx (400, 401, 404, 409, 422...).
 *
 * <p>El {@code GlobalExceptionHandler} escribe el {@code status} directamente
 * en la respuesta HTTP y expone {@code title} + {@code detail} al cliente.
 * No se loguea como error — es un flujo normal del dominio.
 *
 * @param title  nombre corto del error (ej. "Plan no encontrado")
 * @param status código HTTP correspondiente (ej. 404)
 * @param detail mensaje descriptivo para el cliente
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
