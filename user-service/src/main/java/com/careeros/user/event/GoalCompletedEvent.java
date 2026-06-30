package com.careeros.user.event;

import java.util.UUID;

/** Raised when a goal transitions to COMPLETED. */
public record GoalCompletedEvent(UUID userId, UUID goalId, String title) implements DomainEvent {
}
