package com.ej.subscript.infrastructure.adapter.r2dbc.payment;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("payments")
public record PaymentEntity(
        @Id UUID id,
        @Column("subscription_id") UUID subscriptionId,
        BigDecimal amount,
        String currency,
        @Column("paid_at") LocalDateTime paidAt,
        @Column("registered_by") UUID registeredBy
) {}
