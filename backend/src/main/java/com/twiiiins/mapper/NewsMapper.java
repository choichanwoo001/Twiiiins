package com.twiiiins.mapper;

import com.twiiiins.dto.NewsDto;
import com.twiiiins.entity.News;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface NewsMapper {

    NewsDto toDto(News entity);

    @Mapping(target = "id", ignore = true)
    News toEntity(NewsDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(NewsDto dto, @MappingTarget News entity);

    @AfterMapping
    default void ensureCollections(@MappingTarget News entity) {
        if (entity.getImageUrls() == null) {
            entity.setImageUrls(new ArrayList<>());
        }
    }
}

