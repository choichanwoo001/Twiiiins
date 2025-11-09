package com.twiiiins.mapper;

import com.twiiiins.dto.MusicDto;
import com.twiiiins.dto.request.MusicCreateRequest;
import com.twiiiins.dto.request.MusicUpdateRequest;
import com.twiiiins.entity.Music;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MusicMapper {

    MusicDto toDto(Music entity);

    @Mapping(target = "id", ignore = true)
    Music toEntity(MusicCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(MusicUpdateRequest request, @MappingTarget Music entity);
}

