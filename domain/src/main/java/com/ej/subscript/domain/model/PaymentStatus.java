package com.ej.subscript.domain.model;

public enum PaymentStatus {
    PENDING,  // a la espera de confirmación
    PAID,     // pago confirmado por el Owner
    OVERDUE,  // venció sin pagar
    REFUNDED  // devuelto
}
