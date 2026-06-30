package com.careeros.user.event;

import java.util.UUID;

/** Raised after a user profile is updated. */
public record ProfileUpdatedEvent(UUID userId, UUID profileId) implements DomainEvent {
}
