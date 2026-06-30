package com.careeros.user.service.impl;

import com.careeros.user.dto.DashboardSummaryDto;
import com.careeros.user.dto.GoalSummaryDto;
import com.careeros.user.dto.RoadmapTaskResponseDto;
import com.careeros.user.dto.StudyStatsDto;
import com.careeros.user.dto.UserAchievementResponseDto;
import com.careeros.user.entity.GoalStatus;
import com.careeros.user.entity.RoadmapStatus;
import com.careeros.user.entity.SkillProficiency;
import com.careeros.user.mapper.GoalMapper;
import com.careeros.user.mapper.RoadmapTaskMapper;
import com.careeros.user.mapper.UserAchievementMapper;
import com.careeros.user.repository.GoalRepository;
import com.careeros.user.repository.RoadmapRepository;
import com.careeros.user.repository.RoadmapTaskRepository;
import com.careeros.user.repository.UserAchievementRepository;
import com.careeros.user.repository.UserProfileRepository;
import com.careeros.user.service.DashboardService;
import com.careeros.user.service.ProfileCompletionCalculator;
import com.careeros.user.service.StudySessionService;
import com.careeros.user.service.UserSkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserProfileRepository profileRepository;
    private final GoalRepository goalRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapTaskRepository taskRepository;
    private final UserAchievementRepository achievementRepository;
    private final ProfileCompletionCalculator completionCalculator;
    private final UserSkillService userSkillService;
    private final StudySessionService studySessionService;
    private final GoalMapper goalMapper;
    private final RoadmapTaskMapper taskMapper;
    private final UserAchievementMapper achievementMapper;

    @Override
    public DashboardSummaryDto getSummary(UUID userId) {
        int profileCompletion = profileRepository.findByUserId(userId)
                .map(completionCalculator::calculate).orElse(0);

        long activeGoals = goalRepository.countByUserIdAndStatus(userId, GoalStatus.IN_PROGRESS);
        long completedGoals = goalRepository.countByUserIdAndStatus(userId, GoalStatus.COMPLETED);

        Map<SkillProficiency, Long> skillsByProf = userSkillService.countByProficiency(userId);
        Map<String, Long> skillsByProfStr = skillsByProf.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        long totalSkills = skillsByProf.values().stream().mapToLong(Long::longValue).sum();

        long activeRoadmaps = roadmapRepository.countByUserIdAndStatus(userId, RoadmapStatus.ACTIVE);
        int roadmapProgress = 0; // simplified: could aggregate progress % across all active roadmaps

        StudyStatsDto studyStats = studySessionService.stats(userId);

        LocalDate today = LocalDate.now();
        List<RoadmapTaskResponseDto> todayTasks = taskRepository.findTodayTasksForUser(userId, today)
                .stream().map(taskMapper::toResponse).limit(5).toList();

        Pageable upcomingPageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "targetDate"));
        List<GoalSummaryDto> upcomingGoals = goalRepository.findAllByUserId(userId, upcomingPageable)
                .map(goalMapper::toSummary).getContent();

        Pageable recentAchPageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "earnedAt"));
        List<UserAchievementResponseDto> recentAchievements = achievementRepository.findAllByUserId(userId, recentAchPageable)
                .map(achievementMapper::toResponse).getContent();
        long totalAchievements = achievementRepository.countByUserId(userId);

        return new DashboardSummaryDto(
                profileCompletion,
                activeGoals,
                completedGoals,
                skillsByProfStr,
                totalSkills,
                activeRoadmaps,
                roadmapProgress,
                studyStats,
                todayTasks,
                upcomingGoals,
                recentAchievements,
                totalAchievements
        );
    }
}
