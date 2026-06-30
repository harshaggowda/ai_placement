package com.careeros.user.service;

import com.careeros.user.dto.RoadmapCreateDto;
import com.careeros.user.dto.RoadmapResponseDto;
import com.careeros.user.dto.RoadmapTaskCreateDto;
import com.careeros.user.dto.RoadmapTaskResponseDto;
import com.careeros.user.dto.RoadmapTaskUpdateDto;
import com.careeros.user.dto.RoadmapUpdateDto;
import com.careeros.common.pagination.PaginationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RoadmapService {
    RoadmapResponseDto create(UUID userId, RoadmapCreateDto request);
    RoadmapResponseDto get(UUID userId, UUID roadmapId);
    PaginationResponse<RoadmapResponseDto> list(UUID userId, Pageable pageable);
    PaginationResponse<RoadmapResponseDto> listTemplates(Pageable pageable);
    RoadmapResponseDto update(UUID userId, UUID roadmapId, RoadmapUpdateDto request);
    void delete(UUID userId, UUID roadmapId);
    RoadmapTaskResponseDto addTask(UUID userId, UUID roadmapId, RoadmapTaskCreateDto request);
    List<RoadmapTaskResponseDto> listTasks(UUID userId, UUID roadmapId);
    RoadmapTaskResponseDto updateTask(UUID userId, UUID roadmapId, UUID taskId, RoadmapTaskUpdateDto request);
    void deleteTask(UUID userId, UUID roadmapId, UUID taskId);
}
