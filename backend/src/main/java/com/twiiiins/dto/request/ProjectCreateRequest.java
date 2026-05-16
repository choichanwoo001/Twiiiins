package com.twiiiins.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.twiiiins.validation.ValidMediaUrl;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String subtitle;

    private LocalDate premiereDate;

    @Size(max = 255)
    private String location;

    @Size(max = 20)
    private List<@NotBlank @Size(max = 2000) String> descriptions = new ArrayList<>();

    @ValidMediaUrl
    @Size(max = 2048)
    private String coverImageUrl;

    @URL
    @Size(max = 2048)
    private String moreInfoUrl;

    @Size(max = 255)
    private String director;

    @Size(max = 2000)
    private String thankYouText;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]+$", message = "슬러그는 소문자, 숫자, 하이픈만 사용할 수 있습니다.")
    @Size(max = 255)
    private String urlSlug;

    @PositiveOrZero
    private Integer displayOrder;

    @Size(max = 50)
    private List<@NotBlank @ValidMediaUrl @Size(max = 2048) String> imageUrls = new ArrayList<>();

    @Size(max = 20)
    private List<@Valid ReviewRequest> reviews = new ArrayList<>();

    @Getter
    @Setter
    public static class ReviewRequest {

        @NotBlank
        @Size(max = 1000)
        private String text;

        @Size(max = 500)
        private String source;
    }
}


