package com.twiiiins.mapper;

import com.twiiiins.dto.ProjectDto;
import com.twiiiins.dto.request.ProjectCreateRequest;
import com.twiiiins.dto.request.ProjectUpdateRequest;
import com.twiiiins.entity.Project;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectDto toDto(Project entity);

    ProjectDto.ReviewDto toReviewDto(Project.Review review);

    Project.Review toReview(ProjectDto.ReviewDto reviewDto);

    @Mapping(target = "id", ignore = true)
    Project toEntity(ProjectCreateRequest request);

    Project.Review toReview(ProjectCreateRequest.ReviewRequest reviewRequest);

    Project.Review toReview(ProjectUpdateRequest.ReviewRequest reviewRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(ProjectUpdateRequest request, @MappingTarget Project entity);

    @AfterMapping
    default void ensureCollections(@MappingTarget Project entity) {
        if (entity.getDescriptions() == null) {
            entity.setDescriptions(new ArrayList<>());
        }
        if (entity.getImageUrls() == null) {
            entity.setImageUrls(new ArrayList<>());
        }
        if (entity.getReviews() == null) {
            entity.setReviews(new ArrayList<>());
        }
    }
}

