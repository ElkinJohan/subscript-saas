package com.ej.subscript.domain.model;

/**
 * State of a Client within the Owner's business.
 * <p>
 * Modeled as a closed enum to guarantee exhaustiveness: adding a new state
 * requires changing this type and propagating the effects across the
 * domain (allowed transitions) and persistence (mapping to the
 * {@code status} column). Deliberately stricter than an active/inactive
 * boolean, leaving room for future states like
 * {@code PENDING_VERIFICATION} or {@code SUSPENDED} without a type change.
 */
public enum ClientStatus {
    /** Active client — visible in listings and eligible for subscriptions. */
    ACTIVE,
    /** Soft-deleted client; stays in the database but is no longer operated on. */
    INACTIVE
}
