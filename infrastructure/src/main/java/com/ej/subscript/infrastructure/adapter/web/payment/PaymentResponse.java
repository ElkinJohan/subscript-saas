package com.ej.subscript.infrastructure.adapter.web.payment;

import com.ej.subscript.domain.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID subscriptionId,
        BigDecimal amount,
        String currency,
        LocalDateTime paidAt,
        UUID registeredBy
) {
    static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.id(), payment.subscriptionId(),
                payment.amount().amount(), payment.amount().currency(),
                payment.paidAt(), payment.registeredBy()
        );
    }
}
