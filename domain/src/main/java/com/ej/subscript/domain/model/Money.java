package com.ej.subscript.domain.model;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("El monto no puede ser negativo");
        if (currency == null || currency.isBlank())
            throw new IllegalArgumentException("La moneda es obligatoria");
    }

    public static Money cop(BigDecimal amount) {
        return new Money(amount, "COP");
    }
}
