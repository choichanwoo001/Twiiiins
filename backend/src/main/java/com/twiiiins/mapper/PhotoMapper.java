package com.twiiiins.mapper;

import com.twiiiins.dto.PhotoDto;
import com.twiiiins.entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PhotoMapper {

    @Mapping(target = "photoGroupId", source = "photoGroup.id")
    PhotoDto toDto(Photo entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoGroup", ignore = true)
    Photo toEntity(PhotoDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoGroup", ignore = true)
    void updateEntityFromDto(PhotoDto dto, @MappingTarget Photo entity);
}

