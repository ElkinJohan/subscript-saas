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
 * R2DBC representation of the Owner for the {@code owners} table.
 *
 * <p>Implements {@link Persistable} so Spring Data can tell an INSERT from
 * an UPDATE: the {@code isNew} flag is set by {@link OwnerMapper#toEntity}
 * and is {@code @Transient}, so it is not mapped to a column. Without
 * this, Spring Data would assume "already exists" whenever the id comes
 * pre-populated from the domain and would fire UPDATE instead of INSERT.
 *
 * <p>Intentionally mutable (Lombok @Setter) because R2DBC populates the
 * fields via reflection when hydrating a row from the database.
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
