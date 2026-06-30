package com.careeros.user.event;

import java.util.UUID;

/** Raised when all tasks of a roadmap are complete. (Producer lands with the Roadmap vertical.) */
public record RoadmapCompletedEvent(UUID userId, UUID roadmapId) implements DomainEvent {
}
