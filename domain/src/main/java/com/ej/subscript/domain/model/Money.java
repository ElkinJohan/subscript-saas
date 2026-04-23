package com.ej.subscript.domain.model;

import java.math.BigDecimal;

/**
 * Value object que representa un monto monetario con su divisa.
 * Inmutable por diseño — cualquier operación produce una nueva instancia.
 * Usar {@link #cop(BigDecimal)} como atajo para la divisa local del MVP.
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El monto no puede ser negativo");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("La moneda es obligatoria");
    }

    /** Factory para pesos colombianos (COP). */
    public static Money cop(BigDecimal amount) {
        return new Money(amount, "COP");
    }
}
