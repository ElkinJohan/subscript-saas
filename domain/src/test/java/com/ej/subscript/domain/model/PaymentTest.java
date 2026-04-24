package com.ej.subscript.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import com.ej.subscript.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private static final UUID SUBSCRIPTION_ID = UUID.randomUUID();
    private static final UUID REGISTERED_BY = UUID.randomUUID();
    private static final Money AMOUNT = Money.cop(new BigDecimal("50000"));

    @Test
    void shouldRegisterPaymentSuccessfully() {
        Payment payment = Payment.register(SUBSCRIPTION_ID, AMOUNT, REGISTERED_BY);

        assertThat(payment.id()).isNotNull();
        assertThat(payment.subscriptionId()).isEqualTo(SUBSCRIPTION_ID);
        assertThat(payment.amount()).isEqualTo(AMOUNT);
        assertThat(payment.registeredBy()).isEqualTo(REGISTERED_BY);
        assertThat(payment.paidAt()).isNotNull();
    }

    @Test
    void shouldThrowWhenSubscriptionIdIsNull() {
        assertThatThrownBy(() -> Payment.register(null, AMOUNT, REGISTERED_BY))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        assertThatThrownBy(() -> Payment.register(SUBSCRIPTION_ID, null, REGISTERED_BY))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenRegisteredByIsNull() {
        assertThatThrownBy(() -> Payment.register(SUBSCRIPTION_ID, AMOUNT, null))
                .isInstanceOf(BusinessException.class);
    }
}
