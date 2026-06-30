package com.careeros.user.dto;

import java.util.UUID;

/** Payload to start a study session, optionally linked to a roadmap task. */
public record StudySessionStartDto(
        UUID roadmapTaskId,
        String notes
) {
}
