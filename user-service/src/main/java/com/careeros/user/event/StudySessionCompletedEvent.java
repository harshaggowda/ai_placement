package com.careeros.user.event;

import java.util.UUID;

/** Raised when a study session transitions to COMPLETED. */
public record StudySessionCompletedEvent(UUID userId, UUID sessionId, int durationMinutes) implements DomainEvent {
}
