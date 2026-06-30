package com.careeros.user.event;

import java.util.UUID;

/** Raised when a user assigns a skill from the catalog. */
public record SkillAssignedEvent(UUID userId, UUID skillId, String skillName) implements DomainEvent {
}
