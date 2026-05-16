package com.twiiiins.service;

import com.twiiiins.dto.ProjectDto;
import com.twiiiins.dto.request.ProjectCreateRequest;
import com.twiiiins.dto.request.ProjectUpdateRequest;
import com.twiiiins.entity.Project;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.ProjectMapper;
import com.twiiiins.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(projectMapper::toDto)
                .toList();
    }
    
    public List<ProjectDto> getProjectsWithFilters(String title, String location, LocalDate startDate, LocalDate endDate) {
        return projectRepository.findProjectsWithFilters(title, location, startDate, endDate)
                .stream()
                .map(projectMapper::toDto)
                .toList();
    }
    
    public ProjectDto getProjectById(@NonNull Long id) {
        final Project project = projectRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return projectMapper.toDto(project);
    }
    
    public ProjectDto getProjectBySlug(@NonNull String slug) {
        final Project project = projectRepository.findByUrlSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with slug: " + slug));
        return projectMapper.toDto(project);
    }
    
    @Transactional
    public ProjectDto createProject(@NonNull ProjectCreateRequest request) {
        final Project project = Objects.requireNonNull(
                projectMapper.toEntity(request),
                "ProjectMapper.toEntity returned null"
        );
        final Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }
    
    @Transactional
    public ProjectDto updateProject(@NonNull Long id, @NonNull ProjectUpdateRequest request) {
        final Project project = projectRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        projectMapper.updateEntityFromUpdateRequest(request, project);

        final Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }
    
    @Transactional
    public void deleteProject(@NonNull Long id) {
        final Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }
    
}

