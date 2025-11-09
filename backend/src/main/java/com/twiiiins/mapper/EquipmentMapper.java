package com.twiiiins.mapper;

import com.twiiiins.dto.EquipmentDto;
import com.twiiiins.entity.Equipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {

    EquipmentDto toDto(Equipment entity);

    @Mapping(target = "id", ignore = true)
    Equipment toEntity(EquipmentDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(EquipmentDto dto, @MappingTarget Equipment entity);
}

