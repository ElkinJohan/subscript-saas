package com.ej.subscript.infrastructure.adapter.r2dbc.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity implements Persistable<UUID> {

    @Id private UUID id;
    @Column("subscription_id") private UUID subscriptionId;
    private BigDecimal amount;
    private String currency;
    @Column("paid_at") private LocalDateTime paidAt;
    @Column("registered_by") private UUID registeredBy;
    @Transient private boolean isNew;
}
