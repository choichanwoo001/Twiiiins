package com.twiiiins.mapper;

import com.twiiiins.dto.ContactDto;
import com.twiiiins.dto.request.ContactCreateRequest;
import com.twiiiins.dto.request.ContactUpdateRequest;
import com.twiiiins.entity.Contact;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    ContactDto toDto(Contact entity);

    @Mapping(target = "id", ignore = true)
    Contact toEntity(ContactCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(ContactUpdateRequest request, @MappingTarget Contact entity);
}

