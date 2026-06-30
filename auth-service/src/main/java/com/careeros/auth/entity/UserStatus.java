package com.careeros.auth.entity;

/**
 * Lifecycle state of a {@link User} account.
 *
 * <p>Persisted as a string ({@code @Enumerated(EnumType.STRING)}) so ordering changes never corrupt
 * stored data. Authentication/transition logic is out of scope for this phase.
 */
public enum UserStatus {

    /** Registered but email not yet verified — cannot fully authenticate. */
    PENDING_VERIFICATION,

    /** Active, verified account. */
    ACTIVE,

    /** Temporarily blocked by an administrator or risk system. */
    SUSPENDED,

    /** Voluntarily closed by the user; retained (soft-deleted) for audit/billing. */
    DEACTIVATED
}
