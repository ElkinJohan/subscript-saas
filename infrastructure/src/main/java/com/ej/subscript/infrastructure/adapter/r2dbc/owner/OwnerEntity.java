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
