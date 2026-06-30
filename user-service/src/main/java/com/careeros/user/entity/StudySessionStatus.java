package com.careeros.user.entity;

/** Lifecycle state of a {@link StudySession}. Persisted as STRING. */
public enum StudySessionStatus {
    ACTIVE,
    PAUSED,
    COMPLETED,
    ABANDONED
}
