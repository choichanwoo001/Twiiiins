package com.twiiiins.service;

import com.twiiiins.dto.ProjectDto;
import com.twiiiins.entity.Project;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.ProjectMapper;
import com.twiiiins.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
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
    
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return projectMapper.toDto(project);
    }
    
    public ProjectDto getProjectBySlug(String slug) {
        Project project = projectRepository.findByUrlSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with slug: " + slug));
        return projectMapper.toDto(project);
    }
    
    public ProjectDto createProject(ProjectDto projectDto) {
        Project project = projectMapper.toEntity(projectDto);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }
    
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        projectMapper.updateEntityFromDto(projectDto, project);

        Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }
    
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }
    
}

