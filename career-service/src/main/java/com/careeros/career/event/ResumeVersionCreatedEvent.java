package com.careeros.career.event;

import java.util.UUID;

public record ResumeVersionCreatedEvent(
        UUID resumeId,
        UUID userId,
        Integer newVersion,
        String fileUrl
) {
}
