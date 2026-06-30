package com.careeros.user.entity;

/** Lifecycle state of a {@link RoadmapTask}. Persisted as STRING. */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED
}
