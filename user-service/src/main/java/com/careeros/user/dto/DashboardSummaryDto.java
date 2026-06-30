package com.careeros.user.dto;

import java.util.List;
import java.util.Map;

/**
 * Aggregated dashboard data — a single read-model that the frontend renders.
 * Assembled by {@code DashboardService} from multiple repositories in one call.
 */
public record DashboardSummaryDto(
        int profileCompletion,
        long activeGoals,
        long completedGoals,
        Map<String, Long> skillsByProficiency,
        long totalSkills,
        long activeRoadmaps,
        int roadmapProgressPercent,
        StudyStatsDto studyStats,
        List<RoadmapTaskResponseDto> todayTasks,
        List<GoalSummaryDto> upcomingGoals,
        List<UserAchievementResponseDto> recentAchievements,
        long totalAchievements
) {
}
