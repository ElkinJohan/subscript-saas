package com.ej.subscript.infrastructure.adapter.r2dbc.payment;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Payment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {

    private static final Money PRICE = new Money(new BigDecimal("80000"), "COP");
    private static final Payment PAYMENT = Payment.register(UUID.randomUUID(), PRICE, UUID.randomUUID());

    @Test
    void shouldMapDomainToEntity() {
        PaymentEntity entity = PaymentMapper.toEntity(PAYMENT);

        assertThat(entity.getId()).isEqualTo(PAYMENT.id());
        assertThat(entity.getSubscriptionId()).isEqualTo(PAYMENT.subscriptionId());
        assertThat(entity.getAmount()).isEqualByComparingTo(PAYMENT.amount().amount());
        assertThat(entity.getCurrency()).isEqualTo(PAYMENT.amount().currency());
        assertThat(entity.getRegisteredBy()).isEqualTo(PAYMENT.registeredBy());
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void shouldMapEntityToDomain() {
        PaymentEntity entity = PaymentMapper.toEntity(PAYMENT);
        Payment result = PaymentMapper.toDomain(entity);

        assertThat(result.id()).isEqualTo(PAYMENT.id());
        assertThat(result.amount()).isEqualTo(PAYMENT.amount());
        assertThat(result.registeredBy()).isEqualTo(PAYMENT.registeredBy());
    }

    @Test
    void shouldRoundTripWithoutDataLoss() {
        Payment result = PaymentMapper.toDomain(PaymentMapper.toEntity(PAYMENT));
        assertThat(result).isEqualTo(PAYMENT);
    }
}
