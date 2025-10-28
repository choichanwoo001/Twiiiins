package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String title;
    private String subtitle;
    private LocalDate premiereDate;
    private String location;
    private String description1;
    private String description2;
    private String description3;
    private String mainImageUrl;
    private String horizontal1ImageUrl;
    private String horizontal2ImageUrl;
    private String vertical1ImageUrl;
    private String vertical2ImageUrl;
    private String coverImageUrl;
    private String moreInfoUrl;
    private String review1Text;
    private String review1Source;
    private String review2Text;
    private String review2Source;
    private String urlSlug;
    private Integer displayOrder;
}
