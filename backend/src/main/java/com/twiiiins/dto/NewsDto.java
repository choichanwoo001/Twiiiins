package com.twiiiins.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsDto {
    private Long id;
    private LocalDate date;
    private String title;
    @JsonIgnore private String titleBeforeLocalization;
    private String description;
    private Integer displayOrder;
    private String eventWhenEn;
    private String eventWhenDe;
    private String eventLocationEn;
    private String eventLocationDe;
    private String ctaLabelEn;
    private String ctaLabelDe;
    private String ctaUrl;
    private String titleDe;
    private String bodyEn;
    private String bodyDe;
    private String body;
    private String status;
    private String source;
    private boolean archived;
    private Long version;
    private java.util.List<String> videoUrls;

    private List<String> imageUrls = new ArrayList<>();

    public void useLocalizedTitle(String localizedTitle) {
        if (titleBeforeLocalization == null) titleBeforeLocalization = title;
        title = localizedTitle;
    }
}
