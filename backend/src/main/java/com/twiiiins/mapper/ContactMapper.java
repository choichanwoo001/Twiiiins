package com.twiiiins.mapper;

import com.twiiiins.dto.ContactDto;
import com.twiiiins.entity.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    ContactDto toDto(Contact entity);

    @Mapping(target = "id", ignore = true)
    Contact toEntity(ContactDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(ContactDto dto, @MappingTarget Contact entity);
}

