package com.ej.subscript.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record Payment(
        UUID id,
        UUID subscriptionId,
        Money amount,
        LocalDateTime paidAt,
        UUID registeredBy
) {
    public Payment {
        if (subscriptionId == null)
            throw new IllegalArgumentException("El subscriptionId es obligatorio");
        if (amount == null)
            throw new IllegalArgumentException("El monto es obligatorio");
        if (paidAt == null)
            throw new IllegalArgumentException("La fecha de pago es obligatoria");
        if (registeredBy == null)
            throw new IllegalArgumentException("El registrador es obligatorio");
    }

    public static Payment register(UUID subscriptionId, Money amount, UUID registeredBy) {
        return new Payment(UUID.randomUUID(), subscriptionId, amount, LocalDateTime.now(), registeredBy);
    }
}
