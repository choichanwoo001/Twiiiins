package com.twiiiins.mapper;

import com.twiiiins.dto.MusicDto;
import com.twiiiins.entity.Music;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MusicMapper {

    MusicDto toDto(Music entity);

    @Mapping(target = "id", ignore = true)
    Music toEntity(MusicDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(MusicDto dto, @MappingTarget Music entity);
}

