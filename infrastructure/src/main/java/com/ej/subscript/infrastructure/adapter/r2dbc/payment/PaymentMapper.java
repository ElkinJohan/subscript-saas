package com.ej.subscript.infrastructure.adapter.r2dbc.payment;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Payment;

class PaymentMapper {

    static PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(
                payment.id(), payment.subscriptionId(),
                payment.amount().amount(), payment.amount().currency(),
                payment.paidAt(), payment.registeredBy()
        );
    }

    static Payment toDomain(PaymentEntity entity) {
        return new Payment(
                entity.id(), entity.subscriptionId(),
                new Money(entity.amount(), entity.currency()),
                entity.paidAt(), entity.registeredBy()
        );
    }
}
