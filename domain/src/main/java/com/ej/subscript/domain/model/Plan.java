package com.ej.subscript.domain.model;

import java.util.UUID;

public record Plan(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        Money price,
        int durationDays,
        PlanStatus status
) {
    public Plan {
        if (ownerId == null)
            throw new IllegalArgumentException("El ownerId es obligatorio");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre del plan es obligatorio");
        if (price == null)
            throw new IllegalArgumentException("El precio es obligatorio");
        if (durationDays <= 0)
            throw new IllegalArgumentException("La duración debe ser mayor a cero");
        if (status == null)
            throw new IllegalArgumentException("El estado es obligatorio");
    }

    public static Plan create(UUID ownerId, String name, String description, Money price, int durationDays) {
        return new Plan(UUID.randomUUID(), ownerId, name, description, price, durationDays, PlanStatus.ACTIVE);
    }

    public Plan deactivate() {
        return new Plan(id, ownerId, name, description, price, durationDays, PlanStatus.INACTIVE);
    }

    public Plan activate() {
        return new Plan(id, ownerId, name, description, price, durationDays, PlanStatus.ACTIVE);
    }
}
