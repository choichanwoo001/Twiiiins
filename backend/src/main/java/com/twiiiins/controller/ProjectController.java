package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.ProjectDto;
import com.twiiiins.dto.request.ProjectCreateRequest;
import com.twiiiins.dto.request.ProjectImagesUpdateRequest;
import com.twiiiins.dto.request.ProjectUpdateRequest;
import com.twiiiins.service.ProjectService;
import com.twiiiins.util.ResponseUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {
    
    private final ProjectService projectService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDto>>> getAllProjects(
            @RequestParam(required = false) @Size(min = 1, max = 255) String title,
            @RequestParam(required = false) @Size(min = 1, max = 255) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ProjectDto> projects;
        if (title != null || location != null || startDate != null || endDate != null) {
            projects = projectService.getProjectsWithFilters(title, location, startDate, endDate);
        } else {
            projects = projectService.getAllProjects();
        }
        return ResponseUtil.listSuccess(projects, "프로젝트 목록을 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectById(@PathVariable @NonNull Long id) {
        return ResponseUtil.success(projectService.getProjectById(id), "프로젝트 정보를 성공적으로 조회했습니다.");
    }
    
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectBySlug(
            @PathVariable @Pattern(regexp = "^[a-z0-9-]+$", message = "슬러그는 소문자, 숫자, 하이픈만 사용할 수 있습니다.") @NonNull String slug) {
        return ResponseUtil.success(projectService.getProjectBySlug(slug), "프로젝트 정보를 성공적으로 조회했습니다.");
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(@Valid @RequestBody @NonNull ProjectCreateRequest request) {
        return ResponseUtil.created(projectService.createProject(request), "프로젝트가 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody @NonNull ProjectUpdateRequest request) {
        return ResponseUtil.success(projectService.updateProject(id, request), "프로젝트가 성공적으로 수정되었습니다.");
    }

    @PutMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProjectImages(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody @NonNull ProjectImagesUpdateRequest request) {
        return ResponseUtil.success(projectService.updateProjectImages(id, request), "프로젝트 이미지가 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable @NonNull Long id) {
        projectService.deleteProject(id);
        return ResponseUtil.deleted("프로젝트가 성공적으로 삭제되었습니다.");
    }
}

