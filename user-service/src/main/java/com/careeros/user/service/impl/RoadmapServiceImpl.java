package com.careeros.user.service.impl;

import com.careeros.exception.ResourceNotFoundException;
import com.careeros.exception.ValidationException;
import com.careeros.user.dto.RoadmapCreateDto;
import com.careeros.user.dto.RoadmapResponseDto;
import com.careeros.user.dto.RoadmapTaskCreateDto;
import com.careeros.user.dto.RoadmapTaskResponseDto;
import com.careeros.user.dto.RoadmapTaskUpdateDto;
import com.careeros.user.dto.RoadmapUpdateDto;
import com.careeros.user.entity.Roadmap;
import com.careeros.user.entity.RoadmapStatus;
import com.careeros.user.entity.RoadmapTask;
import com.careeros.user.entity.TaskStatus;
import com.careeros.user.event.RoadmapCompletedEvent;
import com.careeros.user.mapper.RoadmapMapper;
import com.careeros.user.mapper.RoadmapTaskMapper;
import com.careeros.user.repository.RoadmapRepository;
import com.careeros.user.repository.RoadmapTaskRepository;
import com.careeros.user.service.RoadmapService;
import com.careeros.common.pagination.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapTaskRepository taskRepository;
    private final RoadmapMapper roadmapMapper;
    private final RoadmapTaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RoadmapResponseDto create(UUID userId, RoadmapCreateDto request) {
        Roadmap roadmap = roadmapMapper.toEntity(request);
        roadmap.setUserId(userId);
        Roadmap saved = roadmapRepository.save(roadmap);
        log.info("Roadmap created: userId={} roadmapId={}", userId, saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoadmapResponseDto get(UUID userId, UUID roadmapId) {
        return toResponse(requireOwned(userId, roadmapId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<RoadmapResponseDto> list(UUID userId, Pageable pageable) {
        return PaginationResponse.from(roadmapRepository.findAllByUserId(userId, pageable)
                .map(this::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<RoadmapResponseDto> listTemplates(Pageable pageable) {
        return PaginationResponse.from(roadmapRepository.findAllByIsTemplateTrue(pageable)
                .map(this::toResponse));
    }

    @Override
    public RoadmapResponseDto update(UUID userId, UUID roadmapId, RoadmapUpdateDto request) {
        Roadmap roadmap = requireOwned(userId, roadmapId);
        RoadmapStatus prev = roadmap.getStatus();
        roadmapMapper.updateEntity(request, roadmap);
        if (roadmap.getStatus() == RoadmapStatus.COMPLETED && prev != RoadmapStatus.COMPLETED) {
            roadmap.setCompletedAt(Instant.now());
            eventPublisher.publishEvent(new RoadmapCompletedEvent(userId, roadmapId));
        }
        return toResponse(roadmap);
    }

    @Override
    public void delete(UUID userId, UUID roadmapId) {
        roadmapRepository.delete(requireOwned(userId, roadmapId));
    }

    @Override
    public RoadmapTaskResponseDto addTask(UUID userId, UUID roadmapId, RoadmapTaskCreateDto request) {
        Roadmap roadmap = requireOwned(userId, roadmapId);
        if (roadmap.getStatus() == RoadmapStatus.COMPLETED) {
            throw new ValidationException("Cannot add tasks to a completed roadmap");
        }
        RoadmapTask task = taskMapper.toEntity(request);
        task.setRoadmap(roadmap);
        RoadmapTask saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoadmapTaskResponseDto> listTasks(UUID userId, UUID roadmapId) {
        requireOwned(userId, roadmapId);
        return taskRepository.findAllByRoadmapIdOrderByOrderIndex(roadmapId)
                .stream().map(taskMapper::toResponse).toList();
    }

    @Override
    public RoadmapTaskResponseDto updateTask(UUID userId, UUID roadmapId, UUID taskId, RoadmapTaskUpdateDto request) {
        requireOwned(userId, roadmapId);
        RoadmapTask task = taskRepository.findByIdAndRoadmapId(taskId, roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("RoadmapTask", "id", taskId));
        TaskStatus prev = task.getStatus();
        taskMapper.updateEntity(request, task);
        if (task.getStatus() == TaskStatus.COMPLETED && prev != TaskStatus.COMPLETED) {
            task.setCompletedAt(Instant.now());
            checkAndCompleteRoadmap(task.getRoadmap(), userId);
        }
        return taskMapper.toResponse(task);
    }

    @Override
    public void deleteTask(UUID userId, UUID roadmapId, UUID taskId) {
        requireOwned(userId, roadmapId);
        RoadmapTask task = taskRepository.findByIdAndRoadmapId(taskId, roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("RoadmapTask", "id", taskId));
        taskRepository.delete(task);
    }

    private void checkAndCompleteRoadmap(Roadmap roadmap, UUID userId) {
        long total = taskRepository.countByRoadmapId(roadmap.getId());
        long done = taskRepository.countByRoadmapIdAndStatus(roadmap.getId(), TaskStatus.COMPLETED)
                + taskRepository.countByRoadmapIdAndStatus(roadmap.getId(), TaskStatus.SKIPPED);
        if (total > 0 && done >= total && roadmap.getStatus() != RoadmapStatus.COMPLETED) {
            roadmap.setStatus(RoadmapStatus.COMPLETED);
            roadmap.setCompletedAt(Instant.now());
            eventPublisher.publishEvent(new RoadmapCompletedEvent(userId, roadmap.getId()));
            log.info("Roadmap auto-completed: roadmapId={}", roadmap.getId());
        }
    }

    private Roadmap requireOwned(UUID userId, UUID roadmapId) {
        return roadmapRepository.findByIdAndUserId(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));
    }

    private RoadmapResponseDto toResponse(Roadmap roadmap) {
        int total = (int) taskRepository.countByRoadmapId(roadmap.getId());
        int completed = (int) taskRepository.countByRoadmapIdAndStatus(roadmap.getId(), TaskStatus.COMPLETED);
        int progress = total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
        return roadmapMapper.toResponse(roadmap, total, completed, progress);
    }
}
