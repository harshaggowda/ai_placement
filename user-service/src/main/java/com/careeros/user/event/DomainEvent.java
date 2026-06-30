package com.careeros.user.event;

import java.util.UUID;

/**
 * Marker for User Service domain events.
 *
 * <p>Published in-process via Spring's {@code ApplicationEventPublisher} today; the same records are
 * the intended payloads for future RabbitMQ/Kafka messages, so the migration is a transport swap.
 */
public interface DomainEvent {

    /** The user the event pertains to. */
    UUID userId();
}
