package com.twiiiins.service;

import com.twiiiins.dto.ProjectDto;
import com.twiiiins.entity.Project;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public List<ProjectDto> getProjectsWithFilters(String title, String location, LocalDate startDate, LocalDate endDate) {
        return projectRepository.findProjectsWithFilters(title, location, startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return convertToDto(project);
    }
    
    public ProjectDto getProjectBySlug(String slug) {
        Project project = projectRepository.findByUrlSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with slug: " + slug));
        return convertToDto(project);
    }
    
    public ProjectDto createProject(ProjectDto projectDto) {
        Project project = convertToEntity(projectDto);
        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }
    
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        project.setTitle(projectDto.getTitle());
        project.setSubtitle(projectDto.getSubtitle());
        project.setPremiereDate(projectDto.getPremiereDate());
        project.setLocation(projectDto.getLocation());
        if (projectDto.getDescriptions() != null) {
            project.setDescriptions(projectDto.getDescriptions());
        } else {
            project.setDescriptions(new ArrayList<>());
        }
        project.setCoverImageUrl(projectDto.getCoverImageUrl());
        project.setMoreInfoUrl(projectDto.getMoreInfoUrl());
        project.setDirector(projectDto.getDirector());
        project.setThankYouText(projectDto.getThankYouText());
        
        // 리뷰 배열 업데이트
        if (projectDto.getReviews() != null) {
            project.setReviews(projectDto.getReviews().stream()
                .map(reviewDto -> new Project.Review(reviewDto.getText(), reviewDto.getSource()))
                .collect(Collectors.toList()));
        } else {
            project.setReviews(new ArrayList<>());
        }
        
        project.setUrlSlug(projectDto.getUrlSlug());
        project.setDisplayOrder(projectDto.getDisplayOrder());
        if (projectDto.getImageUrls() != null) {
            project.setImageUrls(projectDto.getImageUrls());
        }
        
        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }
    
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }
    
    private ProjectDto convertToDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setSubtitle(project.getSubtitle());
        dto.setPremiereDate(project.getPremiereDate());
        dto.setLocation(project.getLocation());
        dto.setDescriptions(project.getDescriptions() != null ? project.getDescriptions() : new ArrayList<>());
        dto.setCoverImageUrl(project.getCoverImageUrl());
        dto.setMoreInfoUrl(project.getMoreInfoUrl());
        dto.setDirector(project.getDirector());
        dto.setThankYouText(project.getThankYouText());
        dto.setUrlSlug(project.getUrlSlug());
        dto.setDisplayOrder(project.getDisplayOrder());
        dto.setImageUrls(project.getImageUrls() != null ? project.getImageUrls() : new ArrayList<>());
        
        // 리뷰 배열 변환
        if (project.getReviews() != null && !project.getReviews().isEmpty()) {
            dto.setReviews(project.getReviews().stream()
                .map(review -> new ProjectDto.ReviewDto(review.getText(), review.getSource()))
                .collect(Collectors.toList()));
        } else {
            dto.setReviews(new ArrayList<>());
        }
        
        return dto;
    }
    
    private Project convertToEntity(ProjectDto projectDto) {
        Project project = new Project();
        project.setTitle(projectDto.getTitle());
        project.setSubtitle(projectDto.getSubtitle());
        project.setPremiereDate(projectDto.getPremiereDate());
        project.setLocation(projectDto.getLocation());
        if (projectDto.getDescriptions() != null) {
            project.setDescriptions(projectDto.getDescriptions());
        } else {
            project.setDescriptions(new ArrayList<>());
        }
        project.setCoverImageUrl(projectDto.getCoverImageUrl());
        project.setMoreInfoUrl(projectDto.getMoreInfoUrl());
        project.setDirector(projectDto.getDirector());
        project.setThankYouText(projectDto.getThankYouText());
        
        // 리뷰 배열 변환
        if (projectDto.getReviews() != null) {
            project.setReviews(projectDto.getReviews().stream()
                .map(reviewDto -> new Project.Review(reviewDto.getText(), reviewDto.getSource()))
                .collect(Collectors.toList()));
        } else {
            project.setReviews(new ArrayList<>());
        }
        
        project.setUrlSlug(projectDto.getUrlSlug());
        project.setDisplayOrder(projectDto.getDisplayOrder());
        if (projectDto.getImageUrls() != null) {
            project.setImageUrls(projectDto.getImageUrls());
        }
        return project;
    }
}

