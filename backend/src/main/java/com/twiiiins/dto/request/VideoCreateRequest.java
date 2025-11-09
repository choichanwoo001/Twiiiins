package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class VideoCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotBlank
    @URL
    @Size(max = 2048)
    private String embedUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


