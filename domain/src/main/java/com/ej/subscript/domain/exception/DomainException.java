package com.ej.subscript.domain.exception;

/**
 * Raíz de la jerarquía de excepciones del dominio.
 * Todas las excepciones conocidas del sistema extienden de aquí,
 * lo que permite capturarlas de forma unificada en el {@code GlobalExceptionHandler}.
 *
 * <ul>
 *   <li>{@link BusinessException} — violación de regla de negocio conocida (4xx)</li>
 *   <li>{@link TechnicalException} — falla de infraestructura inesperada (5xx)</li>
 * </ul>
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
