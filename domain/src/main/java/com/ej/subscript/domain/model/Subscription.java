package com.ej.subscript.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Contrato vigente entre un {@link Client} y un {@link Plan}.
 * Encapsula el período activo, el estado y el precio pactado al momento de la creación.
 * Las operaciones de estado ({@link #cancel()}, {@link #renew(int)}) producen
 * nuevas instancias — el dominio es inmutable por diseño.
 */
public record Subscription(
        UUID id,
        UUID clientId,
        UUID planId,
        SubscriptionPeriod period,
        SubscriptionStatus status,
        Money price
) {
    public Subscription {
        if (clientId == null)
            throw new IllegalArgumentException("El clientId es obligatorio");
        if (planId == null)
            throw new IllegalArgumentException("El planId es obligatorio");
        if (period == null)
            throw new IllegalArgumentException("El período es obligatorio");
        if (status == null)
            throw new IllegalArgumentException("El estado es obligatorio");
        if (price == null)
            throw new IllegalArgumentException("El precio es obligatorio");
    }

    /** Crea una suscripción ACTIVE con el período iniciando hoy. */
    public static Subscription create(UUID clientId, UUID planId, Money price, int durationDays) {
        SubscriptionPeriod period = SubscriptionPeriod.of(LocalDate.now(), durationDays);
        return new Subscription(UUID.randomUUID(), clientId, planId, period, SubscriptionStatus.ACTIVE, price);
    }

    public boolean isExpired() {
        return period.isExpired();
    }

    public boolean isDueForRenewal(int withinDays) {
        return period.isDueForRenewal(withinDays);
    }

    /**
     * Renueva la suscripción: el nuevo período inicia desde {@code endDate} del actual,
     * preservando continuidad sin días huecos.
     */
    public Subscription renew(int durationDays) {
        SubscriptionPeriod newPeriod = SubscriptionPeriod.of(period.endDate(), durationDays);
        return new Subscription(id, clientId, planId, newPeriod, SubscriptionStatus.ACTIVE, price);
    }

    /** Retorna una nueva instancia con estado CANCELLED. No muta el receptor. */
    public Subscription cancel() {
        return new Subscription(id, clientId, planId, period, SubscriptionStatus.CANCELLED, price);
    }
}
