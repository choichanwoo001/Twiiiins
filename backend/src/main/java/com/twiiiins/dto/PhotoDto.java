package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    private Long id;
    private String imageUrl;
    private String thumbnailUrl;
    private String altText;
    private Long photoGroupId;
}
