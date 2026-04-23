package com.ej.subscript.infrastructure.adapter.web.subscription;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubscriptionRequest(
        @NotNull UUID clientId,
        @NotNull UUID planId
) {}
