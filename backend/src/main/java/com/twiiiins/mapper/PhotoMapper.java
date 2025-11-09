package com.twiiiins.mapper;

import com.twiiiins.dto.PhotoDto;
import com.twiiiins.dto.request.PhotoCreateRequest;
import com.twiiiins.dto.request.PhotoUpdateRequest;
import com.twiiiins.entity.Photo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PhotoMapper {

    @Mapping(target = "photoGroupId", source = "photoGroup.id")
    PhotoDto toDto(Photo entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoGroup", ignore = true)
    Photo toEntity(PhotoCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "photoGroup", ignore = true)
    void updateEntityFromUpdateRequest(PhotoUpdateRequest request, @MappingTarget Photo entity);
}

