package com.ej.subscript.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value object que representa el rango temporal de una suscripción.
 * Inmutable. {@code endDate} es exclusivo para cálculos de expiración
 * pero inclusivo para {@link #isDueForRenewal}: una suscripción que vence
 * hoy ({@code daysRemaining == 0}) sí está lista para renovar.
 */
public record SubscriptionPeriod(LocalDate startDate, LocalDate endDate) {

    public SubscriptionPeriod {
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        if (!endDate.isAfter(startDate))
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
    }

    /** Crea el período desde {@code start} más {@code durationDays} días. */
    public static SubscriptionPeriod of(LocalDate start, int durationDays) {
        return new SubscriptionPeriod(start, start.plusDays(durationDays));
    }

    /** {@code true} si hoy es posterior a la fecha de fin. */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /** Días restantes hasta la fecha de fin (puede ser negativo si ya expiró). */
    public long daysUntilExpiration() {
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    /**
     * {@code true} si la suscripción vence dentro de {@code withinDays} días,
     * incluyendo el día de vencimiento ({@code remaining >= 0}).
     */
    public boolean isDueForRenewal(int withinDays) {
        long remaining = daysUntilExpiration();
        return remaining >= 0 && remaining <= withinDays;
    }
}
