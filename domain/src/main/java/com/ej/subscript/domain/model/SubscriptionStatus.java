package com.ej.subscript.domain.model;

public enum SubscriptionStatus {
    PENDING,    // creada, esperando primer pago
    ACTIVE,     // vigente y pago al día
    SUSPENDED,  // venció sin pago, dentro del período de gracia
    EXPIRED,    // se acabó el período de gracia sin pagar
    CANCELLED   // cancelada explícitamente por el Owner
}
