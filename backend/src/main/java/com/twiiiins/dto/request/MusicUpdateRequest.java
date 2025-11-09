package com.twiiiins.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class MusicUpdateRequest {

    @Size(min = 1, max = 255)
    private String title;

    @Size(min = 1, max = 255)
    private String artist;

    @URL
    @Size(max = 2048)
    private String coverUrl;

    @URL
    @Size(max = 2048)
    private String linkUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


