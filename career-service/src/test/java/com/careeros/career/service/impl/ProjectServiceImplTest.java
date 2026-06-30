package com.careeros.career.service.impl;

import com.careeros.career.dto.ProjectDto;
import com.careeros.career.dto.ProjectResponseDto;
import com.careeros.career.entity.Project;
import com.careeros.career.mapper.ProjectMapper;
import com.careeros.career.repository.ProjectRepository;
import com.careeros.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock ProjectRepository projectRepository;
    @Mock ProjectMapper projectMapper;

    @InjectMocks ProjectServiceImpl projectService;

    private final UUID userId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();

    @Test
    void createProjectSucceeds() {
        ProjectDto dto = new ProjectDto("CareerOS", "Placement portal", "Backend Developer", null, null, List.of("Java", "Spring"), null, null, true);
        
        Project project = new Project();
        project.setId(projectId);

        when(projectMapper.toEntity(dto)).thenReturn(project);
        when(projectRepository.save(project)).thenReturn(project);
        
        ProjectResponseDto responseMock = new ProjectResponseDto(projectId, userId, "CareerOS", "Placement portal", "Backend Developer", null, null, List.of("Java", "Spring"), null, null, true, null, null);
        when(projectMapper.toResponse(project)).thenReturn(responseMock);

        ProjectResponseDto result = projectService.createProject(userId, dto);

        verify(projectRepository).save(project);
        assertThat(result.name()).isEqualTo("CareerOS");
    }

    @Test
    void getProjectThrowsWhenNotOwned() {
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(userId, projectId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
