package com.ej.subscript.infrastructure.adapter.r2dbc.subscription;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Table("subscriptions")
public record SubscriptionEntity(
        @Id UUID id,
        @Column("client_id") UUID clientId,
        @Column("plan_id") UUID planId,
        @Column("start_date") LocalDate startDate,
        @Column("end_date") LocalDate endDate,
        String status,
        @Column("price_amount") BigDecimal priceAmount,
        @Column("price_currency") String priceCurrency
) {}
