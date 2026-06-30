package com.careeros.user.service;

import com.careeros.user.dto.NotificationPreferenceDto;
import com.careeros.user.dto.NotificationPreferenceResponseDto;

import java.util.UUID;

public interface NotificationPreferenceService {
    NotificationPreferenceResponseDto get(UUID userId);
    NotificationPreferenceResponseDto upsert(UUID userId, NotificationPreferenceDto request);
}
