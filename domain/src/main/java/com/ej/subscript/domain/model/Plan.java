package com.ej.subscript.domain.model;

import com.ej.subscript.domain.exception.BusinessException;

import java.util.UUID;

/**
 * Plan de membresía ofrecido por un Owner.
 * Define precio, duración y descripción del servicio.
 * Solo los planes con estado {@link PlanStatus#ACTIVE} pueden ser contratados.
 */
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
            throw new BusinessException("Datos inválidos", 422, "El ownerId es obligatorio");
        if (name == null || name.isBlank())
            throw new BusinessException("Datos inválidos", 422, "El nombre del plan es obligatorio");
        if (price == null)
            throw new BusinessException("Datos inválidos", 422, "El precio es obligatorio");
        if (durationDays <= 0)
            throw new BusinessException("Datos inválidos", 422, "La duración debe ser mayor a cero");
        if (status == null)
            throw new BusinessException("Datos inválidos", 422, "El estado es obligatorio");
    }

    /** Crea un nuevo plan con estado ACTIVE. */
    public static Plan create(UUID ownerId, String name, String description, Money price, int durationDays) {
        return new Plan(UUID.randomUUID(), ownerId, name, description, price, durationDays, PlanStatus.ACTIVE);
    }

    /** Retorna una nueva instancia con estado INACTIVE. No muta el receptor. */
    public Plan deactivate() {
        return new Plan(id, ownerId, name, description, price, durationDays, PlanStatus.INACTIVE);
    }

    /** Retorna una nueva instancia con estado ACTIVE. */
    public Plan activate() {
        return new Plan(id, ownerId, name, description, price, durationDays, PlanStatus.ACTIVE);
    }
}
