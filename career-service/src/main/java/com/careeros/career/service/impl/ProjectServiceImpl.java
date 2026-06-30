package com.careeros.career.service.impl;

import com.careeros.career.dto.ProjectDto;
import com.careeros.career.dto.ProjectResponseDto;
import com.careeros.career.entity.Project;
import com.careeros.career.mapper.ProjectMapper;
import com.careeros.career.repository.ProjectRepository;
import com.careeros.career.service.ProjectService;
import com.careeros.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getAllProjects(UUID userId) {
        return projectRepository.findAllByUserId(userId).stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProject(UUID userId, UUID projectId) {
        Project project = getProjectEntity(userId, projectId);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto createProject(UUID userId, ProjectDto dto) {
        Project project = projectMapper.toEntity(dto);
        project.setUserId(userId);

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(UUID userId, UUID projectId, ProjectDto dto) {
        Project project = getProjectEntity(userId, projectId);
        projectMapper.updateEntity(dto, project);

        Project updated = projectRepository.save(project);
        return projectMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        Project project = getProjectEntity(userId, projectId);
        projectRepository.delete(project);
    }

    private Project getProjectEntity(UUID userId, UUID projectId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
}
