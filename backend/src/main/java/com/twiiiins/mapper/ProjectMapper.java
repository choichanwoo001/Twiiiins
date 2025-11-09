package com.twiiiins.mapper;

import com.twiiiins.dto.ProjectDto;
import com.twiiiins.entity.Project;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectDto toDto(Project entity);

    @Mapping(target = "id", ignore = true)
    Project toEntity(ProjectDto dto);

    ProjectDto.ReviewDto toReviewDto(Project.Review review);

    Project.Review toReview(ProjectDto.ReviewDto reviewDto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(ProjectDto dto, @MappingTarget Project entity);

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

