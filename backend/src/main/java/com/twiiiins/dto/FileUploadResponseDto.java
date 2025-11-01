package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponseDto {
    private String url;
    private String thumbnailUrl;
    private String filename;
    private String originalFilename;
    private Long fileSize;
    private String contentType;
}
