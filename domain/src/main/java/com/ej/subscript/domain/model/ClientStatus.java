package com.ej.subscript.domain.model;

/**
 * Estado de un Client en el negocio del Owner.
 * <p>
 * Modelado como enum cerrado para garantizar exhaustividad: agregar un
 * estado nuevo requiere modificar este tipo y propagar los efectos en el
 * dominio (transiciones permitidas) y en la persistencia (mapeo a la
 * columna {@code status}). Es deliberadamente más restrictivo que un
 * boolean activo/inactivo para dejar espacio a estados futuros como
 * {@code PENDING_VERIFICATION} o {@code SUSPENDED} sin un cambio de tipo.
 */
public enum ClientStatus {
    /** Cliente vigente — visible en listados y elegible para suscripciones. */
    ACTIVE,
    /** Cliente desactivado por soft delete; queda en la base pero no se opera con él. */
    INACTIVE
}
