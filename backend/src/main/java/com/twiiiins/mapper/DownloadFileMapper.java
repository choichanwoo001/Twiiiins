package com.twiiiins.mapper;

import com.twiiiins.dto.DownloadFileDto;
import com.twiiiins.dto.request.DownloadFileCreateRequest;
import com.twiiiins.dto.request.DownloadFileUpdateRequest;
import com.twiiiins.entity.DownloadFile;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DownloadFileMapper {

    DownloadFileDto toDto(DownloadFile entity);

    @Mapping(target = "id", ignore = true)
    DownloadFile toEntity(DownloadFileCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromUpdateRequest(DownloadFileUpdateRequest request, @MappingTarget DownloadFile entity);
}

