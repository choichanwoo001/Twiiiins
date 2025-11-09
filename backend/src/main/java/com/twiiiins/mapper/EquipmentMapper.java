package com.twiiiins.mapper;

import com.twiiiins.dto.EquipmentDto;
import com.twiiiins.dto.request.EquipmentCreateRequest;
import com.twiiiins.dto.request.EquipmentUpdateRequest;
import com.twiiiins.entity.Equipment;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {

    EquipmentDto toDto(Equipment entity);

    @Mapping(target = "id", ignore = true)
    Equipment toEntity(EquipmentCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(EquipmentUpdateRequest request, @MappingTarget Equipment entity);
}

