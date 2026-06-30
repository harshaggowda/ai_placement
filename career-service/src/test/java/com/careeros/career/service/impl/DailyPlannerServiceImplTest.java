package com.careeros.career.service.impl;

import com.careeros.career.dto.DailyPlannerDto;
import com.careeros.career.dto.DailyPlannerResponseDto;
import com.careeros.career.entity.DailyPlanner;
import com.careeros.career.mapper.DailyPlannerMapper;
import com.careeros.career.repository.DailyPlannerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyPlannerServiceImplTest {

    @Mock DailyPlannerRepository plannerRepository;
    @Mock DailyPlannerMapper plannerMapper;

    @InjectMocks DailyPlannerServiceImpl plannerService;

    private final UUID userId = UUID.randomUUID();
    private final UUID plannerId = UUID.randomUUID();

    @Test
    void createPlannerSucceeds() {
        LocalDate date = LocalDate.now();
        DailyPlannerDto dto = new DailyPlannerDto(date, "System Design", null, null, false);
        
        DailyPlanner planner = new DailyPlanner();
        planner.setId(plannerId);

        when(plannerRepository.findByTargetDateAndUserId(date, userId)).thenReturn(Optional.empty());
        when(plannerMapper.toEntity(dto)).thenReturn(planner);
        when(plannerRepository.save(planner)).thenReturn(planner);
        
        DailyPlannerResponseDto responseMock = new DailyPlannerResponseDto(plannerId, userId, date, "System Design", null, null, false, null, null);
        when(plannerMapper.toResponse(planner)).thenReturn(responseMock);

        DailyPlannerResponseDto result = plannerService.createPlanner(userId, dto);

        verify(plannerRepository).save(planner);
        assertThat(result.focusArea()).isEqualTo("System Design");
    }

    @Test
    void createPlannerThrowsWhenDateExists() {
        LocalDate date = LocalDate.now();
        DailyPlannerDto dto = new DailyPlannerDto(date, "System Design", null, null, false);
        
        when(plannerRepository.findByTargetDateAndUserId(date, userId)).thenReturn(Optional.of(new DailyPlanner()));

        assertThatThrownBy(() -> plannerService.createPlanner(userId, dto))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
