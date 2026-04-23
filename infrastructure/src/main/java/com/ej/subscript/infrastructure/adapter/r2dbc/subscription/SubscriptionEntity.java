package com.ej.subscript.infrastructure.adapter.r2dbc.subscription;

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
import java.time.LocalDate;
import java.util.UUID;

@Table("subscriptions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionEntity implements Persistable<UUID> {

    @Id private UUID id;
    @Column("client_id") private UUID clientId;
    @Column("plan_id") private UUID planId;
    @Column("start_date") private LocalDate startDate;
    @Column("end_date") private LocalDate endDate;
    private String status;
    @Column("price_amount") private BigDecimal priceAmount;
    @Column("price_currency") private String priceCurrency;
    @Transient private boolean isNew;
}
