package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicDto {
    private Long id;
    private String title;
    private String artist;
    private String coverUrl;
    private String linkUrl;
    private Integer displayOrder;
}
