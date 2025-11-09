package com.twiiiins.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class VideoUpdateRequest {

    @Size(min = 1, max = 255)
    private String title;

    @URL
    @Size(max = 2048)
    private String embedUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


