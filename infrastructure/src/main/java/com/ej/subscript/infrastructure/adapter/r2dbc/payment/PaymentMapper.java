package com.ej.subscript.infrastructure.adapter.r2dbc.payment;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Payment;

class PaymentMapper {

    static PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(
                payment.id(), payment.subscriptionId(),
                payment.amount().amount(), payment.amount().currency(),
                payment.paidAt(), payment.registeredBy(), true
        );
    }

    static Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.getId(), entity.getSubscriptionId(),
                new Money(entity.getAmount(), entity.getCurrency()),
                entity.getPaidAt(), entity.getRegisteredBy()
        );
    }
}
