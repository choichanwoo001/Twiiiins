package com.twiiiins.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class PhotoUpdateRequest {

    @URL
    @Size(max = 2048)
    private String imageUrl;

    @URL
    @Size(max = 2048)
    private String thumbnailUrl;

    @Size(min = 1, max = 255)
    private String altText;
}

