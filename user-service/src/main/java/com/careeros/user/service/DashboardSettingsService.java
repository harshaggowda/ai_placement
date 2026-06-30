package com.careeros.user.service;

import com.careeros.user.dto.DashboardSettingsDto;
import com.careeros.user.dto.DashboardSettingsResponseDto;

import java.util.UUID;

public interface DashboardSettingsService {
    DashboardSettingsResponseDto get(UUID userId);
    DashboardSettingsResponseDto upsert(UUID userId, DashboardSettingsDto request);
}
