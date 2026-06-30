package com.careeros.user.service;

import com.careeros.user.dto.DashboardSummaryDto;

import java.util.UUID;

/** Aggregates data from all User Service domains into a single dashboard read-model. */
public interface DashboardService {
    DashboardSummaryDto getSummary(UUID userId);
}
