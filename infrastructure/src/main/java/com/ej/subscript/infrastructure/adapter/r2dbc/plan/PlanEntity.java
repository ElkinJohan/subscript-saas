package com.ej.subscript.infrastructure.adapter.r2dbc.plan;

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
import java.util.UUID;

@Table("plans")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlanEntity implements Persistable<UUID> {

    @Id private UUID id;
    @Column("owner_id") private UUID ownerId;
    private String name;
    private String description;
    @Column("price_amount") private BigDecimal priceAmount;
    @Column("price_currency") private String priceCurrency;
    @Column("duration_days") private int durationDays;
    private String status;
    @Transient private boolean isNew;
}
