package com.careeros.career.event;

import java.util.UUID;

public record ResumeUploadedEvent(
        UUID resumeId,
        UUID userId,
        String fileUrl
) {
}
