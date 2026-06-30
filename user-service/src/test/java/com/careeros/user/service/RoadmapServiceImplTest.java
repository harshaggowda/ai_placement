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
import com.careeros.user.entity.TaskPriority;
import com.careeros.user.entity.TaskStatus;
import com.careeros.user.event.RoadmapCompletedEvent;
import com.careeros.user.mapper.RoadmapMapper;
import com.careeros.user.mapper.RoadmapTaskMapper;
import com.careeros.user.repository.RoadmapRepository;
import com.careeros.user.repository.RoadmapTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RoadmapServiceImpl}. Covers roadmap and task CRUD, completion rule,
 * auto-completion when all tasks are done/skipped, and ownership enforcement.
 */
@ExtendWith(MockitoExtension.class)
class RoadmapServiceImplTest {

    @Mock RoadmapRepository roadmapRepository;
    @Mock RoadmapTaskRepository taskRepository;
    @Mock RoadmapMapper roadmapMapper;
    @Mock RoadmapTaskMapper taskMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks RoadmapServiceImpl roadmapService;

    private final UUID userId = UUID.randomUUID();
    private final UUID roadmapId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();

    // -----------------------------------------------------------------------
    // create()
    // -----------------------------------------------------------------------

    @Test
    void createAssignsOwnerAndSaves() {
        Roadmap roadmap = new Roadmap();
        when(roadmapMapper.toEntity(any(RoadmapCreateDto.class))).thenReturn(roadmap);
        when(roadmapRepository.save(roadmap)).thenReturn(roadmap);
        when(roadmapMapper.toResponse(any(Roadmap.class), anyInt(), anyInt(), anyInt()))
                .thenReturn(mockRoadmapResponse());

        roadmapService.create(userId, new RoadmapCreateDto(null, "Spring Boot", null, null, null, false));

        assertThat(roadmap.getUserId()).isEqualTo(userId);
        verify(roadmapRepository).save(roadmap);
    }

    // -----------------------------------------------------------------------
    // get()
    // -----------------------------------------------------------------------

    @Test
    void getReturnsMappedResponse() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.ACTIVE);
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));
        when(roadmapMapper.toResponse(any(Roadmap.class), anyInt(), anyInt(), anyInt()))
                .thenReturn(mockRoadmapResponse());

        roadmapService.get(userId, roadmapId);

        verify(roadmapMapper).toResponse(any(Roadmap.class), anyInt(), anyInt(), anyInt());
    }

    @Test
    void getThrowsNotFoundForWrongOwner() {
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roadmapService.get(userId, roadmapId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // update() — completion rule
    // -----------------------------------------------------------------------

    @Test
    void updateToCompletedStatusPublishesEventAndStampsTime() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.ACTIVE);
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));
        when(roadmapMapper.toResponse(any(Roadmap.class), anyInt(), anyInt(), anyInt()))
                .thenReturn(mockRoadmapResponse());
        doAnswer(inv -> { roadmap.setStatus(RoadmapStatus.COMPLETED); return null; })
                .when(roadmapMapper).updateEntity(any(), any());

        roadmapService.update(userId, roadmapId,
                new RoadmapUpdateDto(null, null, null, RoadmapStatus.COMPLETED, null, null));

        assertThat(roadmap.getCompletedAt()).isNotNull();
        verify(eventPublisher).publishEvent(any(RoadmapCompletedEvent.class));
    }

    @Test
    void updatingAlreadyCompletedRoadmapDoesNotFireSecondEvent() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.COMPLETED);
        roadmap.setCompletedAt(Instant.now().minusSeconds(600));
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));
        when(roadmapMapper.toResponse(any(Roadmap.class), anyInt(), anyInt(), anyInt()))
                .thenReturn(mockRoadmapResponse());
        doNothing().when(roadmapMapper).updateEntity(any(), any());

        roadmapService.update(userId, roadmapId,
                new RoadmapUpdateDto(null, "New title", null, null, null, null));

        verify(eventPublisher, never()).publishEvent(any());
    }

    // -----------------------------------------------------------------------
    // delete()
    // -----------------------------------------------------------------------

    @Test
    void deleteHappyPath() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.ACTIVE);
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));

        roadmapService.delete(userId, roadmapId);

        verify(roadmapRepository).delete(roadmap);
    }

    // -----------------------------------------------------------------------
    // addTask()
    // -----------------------------------------------------------------------

    @Test
    void addTaskToCompletedRoadmapThrowsValidation() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.COMPLETED);
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));

        assertThatThrownBy(() -> roadmapService.addTask(userId, roadmapId,
                new RoadmapTaskCreateDto("Task", null, TaskPriority.MEDIUM, 0, null, null, null)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("completed roadmap");
    }

    @Test
    void addTaskToActiveRoadmapSavesTask() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.ACTIVE);
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));
        RoadmapTask task = new RoadmapTask();
        when(taskMapper.toEntity(any(RoadmapTaskCreateDto.class))).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(mockTaskResponse());

        roadmapService.addTask(userId, roadmapId,
                new RoadmapTaskCreateDto("Read docs", null, TaskPriority.HIGH, 0, null, null, null));

        verify(taskRepository).save(task);
        assertThat(task.getRoadmap()).isEqualTo(roadmap);
    }

    // -----------------------------------------------------------------------
    // updateTask() — auto-complete
    // -----------------------------------------------------------------------

    @Test
    void completingLastTaskAutoCompletesRoadmap() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.ACTIVE);
        roadmap.setId(roadmapId);
        RoadmapTask task = new RoadmapTask();
        task.setRoadmap(roadmap);
        task.setStatus(TaskStatus.TODO);
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));
        when(taskRepository.findByIdAndRoadmapId(taskId, roadmapId)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(mockTaskResponse());
        doAnswer(inv -> { task.setStatus(TaskStatus.COMPLETED); return null; })
                .when(taskMapper).updateEntity(any(), any());
        when(taskRepository.countByRoadmapId(roadmapId)).thenReturn(1L);
        when(taskRepository.countByRoadmapIdAndStatus(roadmapId, TaskStatus.COMPLETED)).thenReturn(1L);
        when(taskRepository.countByRoadmapIdAndStatus(roadmapId, TaskStatus.SKIPPED)).thenReturn(0L);

        roadmapService.updateTask(userId, roadmapId, taskId,
                new RoadmapTaskUpdateDto(null, null, TaskStatus.COMPLETED, null, null, null, null, null, null));

        assertThat(roadmap.getStatus()).isEqualTo(RoadmapStatus.COMPLETED);
        verify(eventPublisher).publishEvent(any(RoadmapCompletedEvent.class));
    }

    @Test
    void updatingNonExistentTaskThrowsNotFound() {
        Roadmap roadmap = makeRoadmap(RoadmapStatus.ACTIVE);
        when(roadmapRepository.findByIdAndUserId(roadmapId, userId)).thenReturn(Optional.of(roadmap));
        when(taskRepository.findByIdAndRoadmapId(taskId, roadmapId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roadmapService.updateTask(userId, roadmapId, taskId,
                new RoadmapTaskUpdateDto(null, null, TaskStatus.COMPLETED, null, null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Roadmap makeRoadmap(RoadmapStatus status) {
        Roadmap r = new Roadmap();
        r.setId(roadmapId);
        r.setUserId(userId);
        r.setTitle("Test Roadmap");
        r.setStatus(status);
        return r;
    }

    private static RoadmapResponseDto mockRoadmapResponse() {
        return new RoadmapResponseDto(UUID.randomUUID(), UUID.randomUUID(), null,
                "Test", null, RoadmapStatus.ACTIVE, null, null, null, false,
                0, 0, 0, Instant.now(), Instant.now());
    }

    private static RoadmapTaskResponseDto mockTaskResponse() {
        return new RoadmapTaskResponseDto(UUID.randomUUID(), UUID.randomUUID(), "Task",
                null, TaskStatus.TODO, null, 0, null, null, null, null, null, Instant.now());
    }
}
