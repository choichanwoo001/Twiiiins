package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String title;
    private String subtitle;
    private LocalDate premiereDate;
    private String location;
    private List<String> descriptions = new ArrayList<>();
    private String coverImageUrl;
    private String moreInfoUrl;
    private String director;
    private String thankYouText;
    private String urlSlug;
    private Integer displayOrder;
    private List<String> imageUrls = new ArrayList<>();
    
    // 리뷰 배열 (새로운 방식)
    private List<ReviewDto> reviews = new ArrayList<>();
    
    // Review DTO
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDto {
        private String text;
        private String source;
    }
}
