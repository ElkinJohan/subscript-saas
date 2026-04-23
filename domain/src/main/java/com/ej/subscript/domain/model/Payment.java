package com.ej.subscript.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de un pago realizado contra una suscripción activa.
 * Es inmutable: los pagos no se modifican, solo se crean.
 * {@code registeredBy} es el UUID del owner que registra el cobro manualmente.
 */
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

    /**
     * Registra un nuevo pago con la fecha/hora actual.
     * El monto se toma del precio de la suscripción, no del request HTTP.
     */
    public static Payment register(UUID subscriptionId, Money amount, UUID registeredBy) {
        return new Payment(UUID.randomUUID(), subscriptionId, amount, LocalDateTime.now(), registeredBy);
    }
}
