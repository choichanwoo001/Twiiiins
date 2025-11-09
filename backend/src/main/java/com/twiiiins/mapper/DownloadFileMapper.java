package com.twiiiins.mapper;

import com.twiiiins.dto.DownloadFileDto;
import com.twiiiins.entity.DownloadFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DownloadFileMapper {

    DownloadFileDto toDto(DownloadFile entity);

    @Mapping(target = "id", ignore = true)
    DownloadFile toEntity(DownloadFileDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(DownloadFileDto dto, @MappingTarget DownloadFile entity);
}

