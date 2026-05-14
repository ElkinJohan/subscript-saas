package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;

/**
 * Traductor entre el modelo de dominio {@link Owner} y la entidad R2DBC
 * {@link OwnerEntity}. Mantenerlo en un tipo aparte mantiene a la entity y al
 * domain record desacoplados: ninguno conoce al otro.
 */
class OwnerMapper {

    /**
     * Hacia la persistencia. Marca {@code isNew = true} para que Spring Data
     * emita un INSERT — ver {@link OwnerEntity} para el detalle.
     */
    static OwnerEntity toEntity(Owner owner) {
        OwnerEntity e = new OwnerEntity(
                owner.id(), owner.nit(), owner.name(), owner.email(),
                owner.phone(), owner.businessName(), owner.gracePeriodDays(),
                owner.passwordHash(), true
        );
        return e;
    }

    /**
     * Hacia el dominio. El compact constructor del record valida invariantes,
     * así que una fila corrupta en la base se rechaza al hidratar — no llega
     * a la capa de aplicación en estado inválido.
     */
    static Owner toDomain(OwnerEntity entity) {
        return new Owner(
                entity.getId(), entity.getNit(), entity.getName(), entity.getEmail(),
                entity.getPhone(), entity.getBusinessName(), entity.getGracePeriodDays(),
                entity.getPasswordHash()
        );
    }
}
