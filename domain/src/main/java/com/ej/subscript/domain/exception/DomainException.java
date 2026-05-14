package com.ej.subscript.domain.exception;

/**
 * Root of the domain exception hierarchy. Every expected exception in the
 * system extends from this type, which lets the {@code GlobalExceptionHandler}
 * catch them uniformly.
 *
 * <ul>
 *   <li>{@link BusinessException} — known business-rule violation (4xx)</li>
 *   <li>{@link TechnicalException} — unexpected infrastructure failure (5xx)</li>
 * </ul>
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
