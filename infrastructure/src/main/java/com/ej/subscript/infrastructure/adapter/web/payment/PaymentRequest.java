package com.ej.subscript.infrastructure.adapter.web.payment;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentRequest(
        @NotNull UUID subscriptionId,
        @NotNull UUID registeredBy
) {}
