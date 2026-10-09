package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.twiiiins.validation.ValidMediaUrl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NewsCreateRequest {

    @NotNull
    private LocalDate date;

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 1000)
    private String description;

    @PositiveOrZero
    private Integer displayOrder;
    @Size(max = 255) private String eventWhenEn;
    @Size(max = 255) private String eventWhenDe;
    @Size(max = 255) private String eventLocationEn;
    @Size(max = 255) private String eventLocationDe;
    @Size(max = 255) private String ctaLabelEn;
    @Size(max = 255) private String ctaLabelDe;
    @Size(max = 2048) private String ctaUrl;
    @Size(max = 255) private String titleDe;
    @Size(max = 100000) private String bodyEn;
    @Size(max = 100000) private String bodyDe;
    private Long version;
    @Size(max = 20) private java.util.List<@NotBlank @Size(max = 2048) String> videoUrls;


    @Size(max = 50)
    private List<@NotBlank @ValidMediaUrl @Size(max = 2048) String> imageUrls = new ArrayList<>();
}


