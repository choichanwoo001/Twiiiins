package com.twiiiins.mapper;

import com.twiiiins.dto.ConcertDto;
import com.twiiiins.dto.request.ConcertCreateRequest;
import com.twiiiins.dto.request.ConcertUpdateRequest;
import com.twiiiins.entity.Concert;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ConcertMapper {

    ConcertDto toDto(Concert entity);

    @Mapping(target = "id", ignore = true)
    Concert toEntity(ConcertCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(ConcertUpdateRequest request, @MappingTarget Concert entity);
}

