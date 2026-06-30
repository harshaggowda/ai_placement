package com.careeros.career.service;

import com.careeros.career.dto.ProjectDto;
import com.careeros.career.dto.ProjectResponseDto;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    List<ProjectResponseDto> getAllProjects(UUID userId);

    ProjectResponseDto getProject(UUID userId, UUID projectId);

    ProjectResponseDto createProject(UUID userId, ProjectDto dto);

    ProjectResponseDto updateProject(UUID userId, UUID projectId, ProjectDto dto);

    void deleteProject(UUID userId, UUID projectId);
}
