package com.careeros.user.event;

import java.util.UUID;

/** Raised after a user profile is created. */
public record ProfileCreatedEvent(UUID userId, UUID profileId) implements DomainEvent {
}
