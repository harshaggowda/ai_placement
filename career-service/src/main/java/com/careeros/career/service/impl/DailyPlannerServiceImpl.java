package com.careeros.career.service.impl;

import com.careeros.career.dto.DailyPlannerDto;
import com.careeros.career.dto.DailyPlannerResponseDto;
import com.careeros.career.entity.DailyPlanner;
import com.careeros.career.mapper.DailyPlannerMapper;
import com.careeros.career.repository.DailyPlannerRepository;
import com.careeros.career.service.DailyPlannerService;
import com.careeros.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyPlannerServiceImpl implements DailyPlannerService {

    private final DailyPlannerRepository dailyPlannerRepository;
    private final DailyPlannerMapper dailyPlannerMapper;

    @Override
    @Transactional(readOnly = true)
    public List<DailyPlannerResponseDto> getAllPlanners(UUID userId) {
        return dailyPlannerRepository.findAllByUserIdOrderByTargetDateDesc(userId).stream()
                .map(dailyPlannerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DailyPlannerResponseDto getPlanner(UUID userId, UUID plannerId) {
        DailyPlanner planner = getPlannerEntity(userId, plannerId);
        return dailyPlannerMapper.toResponse(planner);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DailyPlannerResponseDto getPlannerByDate(UUID userId, LocalDate date) {
        DailyPlanner planner = dailyPlannerRepository.findByTargetDateAndUserId(date, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planner not found for date: " + date));
        return dailyPlannerMapper.toResponse(planner);
    }

    @Override
    @Transactional
    public DailyPlannerResponseDto createPlanner(UUID userId, DailyPlannerDto dto) {
        if (dailyPlannerRepository.findByTargetDateAndUserId(dto.targetDate(), userId).isPresent()) {
            throw new IllegalArgumentException("A planner for this date already exists");
        }
        
        DailyPlanner planner = dailyPlannerMapper.toEntity(dto);
        planner.setUserId(userId);

        DailyPlanner saved = dailyPlannerRepository.save(planner);
        return dailyPlannerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DailyPlannerResponseDto updatePlanner(UUID userId, UUID plannerId, DailyPlannerDto dto) {
        DailyPlanner planner = getPlannerEntity(userId, plannerId);
        
        if (!planner.getTargetDate().equals(dto.targetDate()) && 
            dailyPlannerRepository.findByTargetDateAndUserId(dto.targetDate(), userId).isPresent()) {
            throw new IllegalArgumentException("A planner for this new date already exists");
        }
        
        dailyPlannerMapper.updateEntity(dto, planner);

        DailyPlanner updated = dailyPlannerRepository.save(planner);
        return dailyPlannerMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deletePlanner(UUID userId, UUID plannerId) {
        DailyPlanner planner = getPlannerEntity(userId, plannerId);
        dailyPlannerRepository.delete(planner);
    }

    private DailyPlanner getPlannerEntity(UUID userId, UUID plannerId) {
        return dailyPlannerRepository.findByIdAndUserId(plannerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Planner not found"));
    }
}
