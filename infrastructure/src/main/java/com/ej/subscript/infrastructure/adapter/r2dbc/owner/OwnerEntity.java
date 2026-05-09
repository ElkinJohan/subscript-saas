package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Representación R2DBC del Owner para la tabla {@code owners}.
 *
 * <p>Implementa {@link Persistable} para que Spring Data sepa diferenciar un
 * INSERT de un UPDATE: el flag {@code isNew} se setea desde
 * {@link OwnerMapper#toEntity} y es {@code @Transient}, así no se mapea a una
 * columna. Sin esto, Spring Data asume "ya existe" cuando el id viene seteado
 * desde el dominio y dispara UPDATE en vez de INSERT.
 *
 * <p>Es deliberadamente mutable (Lombok @Setter) porque R2DBC popula los
 * campos por reflexión al hidratar la fila desde la base.
 */
@Table("owners")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OwnerEntity implements Persistable<UUID> {

    @Id private UUID id;
    private String nit;
    private String name;
    private String email;
    private String phone;
    @Column("business_name") private String businessName;
    @Column("grace_period_days") private int gracePeriodDays;
    @Column("password_hash") private String passwordHash;
    @Transient private boolean isNew;
}
