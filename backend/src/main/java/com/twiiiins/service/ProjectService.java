package com.twiiiins.service;

import com.twiiiins.dto.ProjectDto;
import com.twiiiins.entity.Project;
import com.twiiiins.exception.ResourceNotFoundException;
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
        project.setDescription1(projectDto.getDescription1());
        project.setDescription2(projectDto.getDescription2());
        project.setDescription3(projectDto.getDescription3());
        project.setMainImageUrl(projectDto.getMainImageUrl());
        project.setHorizontal1ImageUrl(projectDto.getHorizontal1ImageUrl());
        project.setHorizontal2ImageUrl(projectDto.getHorizontal2ImageUrl());
        project.setVertical1ImageUrl(projectDto.getVertical1ImageUrl());
        project.setVertical2ImageUrl(projectDto.getVertical2ImageUrl());
        project.setCoverImageUrl(projectDto.getCoverImageUrl());
        project.setMoreInfoUrl(projectDto.getMoreInfoUrl());
        project.setReview1Text(projectDto.getReview1Text());
        project.setReview1Source(projectDto.getReview1Source());
        project.setReview2Text(projectDto.getReview2Text());
        project.setReview2Source(projectDto.getReview2Source());
        project.setUrlSlug(projectDto.getUrlSlug());
        project.setDisplayOrder(projectDto.getDisplayOrder());
        
        Project savedProject = projectRepository.save(project);
        return convertToDto(savedProject);
    }
    
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }
    
    private ProjectDto convertToDto(Project project) {
        return new ProjectDto(
            project.getId(),
            project.getTitle(),
            project.getSubtitle(),
            project.getPremiereDate(),
            project.getLocation(),
            project.getDescription1(),
            project.getDescription2(),
            project.getDescription3(),
            project.getMainImageUrl(),
            project.getHorizontal1ImageUrl(),
            project.getHorizontal2ImageUrl(),
            project.getVertical1ImageUrl(),
            project.getVertical2ImageUrl(),
            project.getCoverImageUrl(),
            project.getMoreInfoUrl(),
            project.getReview1Text(),
            project.getReview1Source(),
            project.getReview2Text(),
            project.getReview2Source(),
            project.getUrlSlug(),
            project.getDisplayOrder()
        );
    }
    
    private Project convertToEntity(ProjectDto projectDto) {
        Project project = new Project();
        project.setTitle(projectDto.getTitle());
        project.setSubtitle(projectDto.getSubtitle());
        project.setPremiereDate(projectDto.getPremiereDate());
        project.setLocation(projectDto.getLocation());
        project.setDescription1(projectDto.getDescription1());
        project.setDescription2(projectDto.getDescription2());
        project.setDescription3(projectDto.getDescription3());
        project.setMainImageUrl(projectDto.getMainImageUrl());
        project.setHorizontal1ImageUrl(projectDto.getHorizontal1ImageUrl());
        project.setHorizontal2ImageUrl(projectDto.getHorizontal2ImageUrl());
        project.setVertical1ImageUrl(projectDto.getVertical1ImageUrl());
        project.setVertical2ImageUrl(projectDto.getVertical2ImageUrl());
        project.setCoverImageUrl(projectDto.getCoverImageUrl());
        project.setMoreInfoUrl(projectDto.getMoreInfoUrl());
        project.setReview1Text(projectDto.getReview1Text());
        project.setReview1Source(projectDto.getReview1Source());
        project.setReview2Text(projectDto.getReview2Text());
        project.setReview2Source(projectDto.getReview2Source());
        project.setUrlSlug(projectDto.getUrlSlug());
        project.setDisplayOrder(projectDto.getDisplayOrder());
        return project;
    }
}

