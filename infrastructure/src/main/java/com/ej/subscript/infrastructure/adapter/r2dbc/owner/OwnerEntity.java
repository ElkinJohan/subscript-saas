package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("owners")
public record OwnerEntity(
        @Id UUID id,
        String nit,
        String name,
        String email,
        String phone,
        @Column("business_name") String businessName,
        @Column("grace_period_days") int gracePeriodDays
) {}
