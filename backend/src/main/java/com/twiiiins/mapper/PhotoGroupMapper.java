package com.twiiiins.mapper;

import com.twiiiins.dto.PhotoGroupDto;
import com.twiiiins.dto.request.PhotoGroupCreateRequest;
import com.twiiiins.dto.request.PhotoGroupUpdateRequest;
import com.twiiiins.entity.PhotoGroup;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.ArrayList;

@Mapper(componentModel = "spring", uses = PhotoMapper.class)
public interface PhotoGroupMapper {

    @Mapping(target = "photos", ignore = true)
    PhotoGroupDto toDto(PhotoGroup entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photos", ignore = true)
    PhotoGroup toEntity(PhotoGroupCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photos", ignore = true)
    void updateEntityFromUpdateRequest(PhotoGroupUpdateRequest request, @MappingTarget PhotoGroup entity);

    @AfterMapping
    default void ensurePhotosCollection(@MappingTarget PhotoGroup entity) {
        if (entity.getPhotos() == null) {
            entity.setPhotos(new ArrayList<>());
        }
    }
}

