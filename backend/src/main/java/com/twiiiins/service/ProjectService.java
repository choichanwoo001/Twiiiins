package com.twiiiins.service;

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
    
    public List<Project> getAllProjects() {
        return projectRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public List<Project> getProjectsWithFilters(String title, String location, LocalDate startDate, LocalDate endDate) {
        return projectRepository.findProjectsWithFilters(title, location, startDate, endDate);
    }
    
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }
    
    public Project getProjectBySlug(String slug) {
        return projectRepository.findByUrlSlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with slug: " + slug));
    }
    
    public Project createProject(Project project) {
        return projectRepository.save(project);
    }
    
    public Project updateProject(Long id, Project projectDetails) {
        Project project = getProjectById(id);
        
        project.setTitle(projectDetails.getTitle());
        project.setSubtitle(projectDetails.getSubtitle());
        project.setPremiereDate(projectDetails.getPremiereDate());
        project.setLocation(projectDetails.getLocation());
        project.setDescription1(projectDetails.getDescription1());
        project.setDescription2(projectDetails.getDescription2());
        project.setDescription3(projectDetails.getDescription3());
        project.setMainImageUrl(projectDetails.getMainImageUrl());
        project.setHorizontal1ImageUrl(projectDetails.getHorizontal1ImageUrl());
        project.setHorizontal2ImageUrl(projectDetails.getHorizontal2ImageUrl());
        project.setVertical1ImageUrl(projectDetails.getVertical1ImageUrl());
        project.setVertical2ImageUrl(projectDetails.getVertical2ImageUrl());
        project.setCoverImageUrl(projectDetails.getCoverImageUrl());
        project.setMoreInfoUrl(projectDetails.getMoreInfoUrl());
        project.setReview1Text(projectDetails.getReview1Text());
        project.setReview1Source(projectDetails.getReview1Source());
        project.setReview2Text(projectDetails.getReview2Text());
        project.setReview2Source(projectDetails.getReview2Source());
        project.setUrlSlug(projectDetails.getUrlSlug());
        project.setDisplayOrder(projectDetails.getDisplayOrder());
        
        return projectRepository.save(project);
    }
    
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
    }
}

