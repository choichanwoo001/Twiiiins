package com.twiiiins.mapper;

import com.twiiiins.dto.ConcertDto;
import com.twiiiins.entity.Concert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConcertMapper {

    ConcertDto toDto(Concert entity);

    @Mapping(target = "id", ignore = true)
    Concert toEntity(ConcertDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(ConcertDto dto, @MappingTarget Concert entity);
}

