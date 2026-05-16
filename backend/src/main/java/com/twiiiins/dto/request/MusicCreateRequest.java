package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.twiiiins.validation.ValidMediaUrl;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class MusicCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @Size(max = 255)
    private String artist;

    @NotBlank
    @ValidMediaUrl
    @Size(max = 2048)
    private String coverUrl;

    @URL
    @Size(max = 2048)
    private String linkUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


