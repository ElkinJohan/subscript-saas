package com.ej.subscript.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record SubscriptionPeriod(LocalDate startDate, LocalDate endDate) {

    public SubscriptionPeriod {
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        if (!endDate.isAfter(startDate))
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio");
    }

    public static SubscriptionPeriod of(LocalDate start, int durationDays) {
        return new SubscriptionPeriod(start, start.plusDays(durationDays));
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    public long daysUntilExpiration() {
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    public boolean isDueForRenewal(int withinDays) {
        long remaining = daysUntilExpiration();
        return remaining >= 0 && remaining <= withinDays;
    }
}
