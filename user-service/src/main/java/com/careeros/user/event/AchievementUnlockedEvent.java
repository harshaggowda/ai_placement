package com.careeros.user.event;

import java.util.UUID;

/** Raised when a user unlocks an achievement. (Producer lands with the Achievement vertical.) */
public record AchievementUnlockedEvent(UUID userId, UUID achievementId, String code) implements DomainEvent {
}
