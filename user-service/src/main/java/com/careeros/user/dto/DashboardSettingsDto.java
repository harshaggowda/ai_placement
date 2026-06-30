package com.careeros.user.dto;

import com.careeros.user.entity.Theme;
import jakarta.validation.constraints.Size;

public record DashboardSettingsDto(
        Theme theme,
        @Size(max = 10) String language,
        @Size(max = 60) String timezone,
        Boolean compactMode,
        String widgetsConfig
) {
}
