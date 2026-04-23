package com.ej.subscript.infrastructure.adapter.r2dbc.plan;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("plans")
public record PlanEntity(
        @Id UUID id,
        @Column("owner_id") UUID ownerId,
        String name,
        String description,
        @Column("price_amount") BigDecimal priceAmount,
        @Column("price_currency") String priceCurrency,
        @Column("duration_days") int durationDays,
        String status
) {}
