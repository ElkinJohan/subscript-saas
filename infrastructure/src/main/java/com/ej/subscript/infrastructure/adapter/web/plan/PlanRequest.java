package com.ej.subscript.infrastructure.adapter.web.plan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PlanRequest(
        @NotBlank String name,
        String description,
        @NotNull @Positive BigDecimal priceAmount,
        @NotBlank String priceCurrency,
        @Positive int durationDays
) {}
