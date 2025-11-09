package com.twiiiins.mapper;

import com.twiiiins.dto.VideoDto;
import com.twiiiins.dto.request.VideoCreateRequest;
import com.twiiiins.dto.request.VideoUpdateRequest;
import com.twiiiins.entity.Video;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    VideoDto toDto(Video entity);

    @Mapping(target = "id", ignore = true)
    Video toEntity(VideoCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(VideoUpdateRequest request, @MappingTarget Video entity);
}

