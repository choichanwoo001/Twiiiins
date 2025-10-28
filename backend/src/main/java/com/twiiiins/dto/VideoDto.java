package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoDto {
    private Long id;
    private String title;
    private String embedUrl;
    private Integer displayOrder;
}
