package com.twiiiins.mapper;

import com.twiiiins.dto.VideoDto;
import com.twiiiins.entity.Video;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    VideoDto toDto(Video entity);

    @Mapping(target = "id", ignore = true)
    Video toEntity(VideoDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(VideoDto dto, @MappingTarget Video entity);
}

