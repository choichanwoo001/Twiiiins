package com.twiiiins.mapper;

import com.twiiiins.dto.NewsDto;
import com.twiiiins.dto.request.NewsCreateRequest;
import com.twiiiins.dto.request.NewsUpdateRequest;
import com.twiiiins.entity.News;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface NewsMapper {

    NewsDto toDto(News entity);

    @Mapping(target = "id", ignore = true)
    News toEntity(NewsCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(NewsUpdateRequest request, @MappingTarget News entity);

    @AfterMapping
    default void ensureCollections(@MappingTarget News entity) {
        if (entity.getImageUrls() == null) {
            entity.setImageUrls(new ArrayList<>());
        }
    }
}

