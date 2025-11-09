package com.twiiiins.mapper;

import com.twiiiins.dto.PhotoGroupDto;
import com.twiiiins.entity.PhotoGroup;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.ArrayList;

@Mapper(componentModel = "spring", uses = PhotoMapper.class)
public interface PhotoGroupMapper {

    @Mapping(target = "photos", ignore = true)
    PhotoGroupDto toDto(PhotoGroup entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photos", ignore = true)
    PhotoGroup toEntity(PhotoGroupDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photos", ignore = true)
    void updateEntityFromDto(PhotoGroupDto dto, @MappingTarget PhotoGroup entity);

    @AfterMapping
    default void ensurePhotosCollection(@MappingTarget PhotoGroup entity) {
        if (entity.getPhotos() == null) {
            entity.setPhotos(new ArrayList<>());
        }
    }
}

