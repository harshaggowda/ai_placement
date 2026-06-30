package com.careeros.career.service;

import com.careeros.career.dto.DailyPlannerDto;
import com.careeros.career.dto.DailyPlannerResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DailyPlannerService {

    List<DailyPlannerResponseDto> getAllPlanners(UUID userId);

    DailyPlannerResponseDto getPlanner(UUID userId, UUID plannerId);
    
    DailyPlannerResponseDto getPlannerByDate(UUID userId, LocalDate date);

    DailyPlannerResponseDto createPlanner(UUID userId, DailyPlannerDto dto);

    DailyPlannerResponseDto updatePlanner(UUID userId, UUID plannerId, DailyPlannerDto dto);

    void deletePlanner(UUID userId, UUID plannerId);
}
