package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class PhotoCreateRequest {

    @NotBlank
    @URL
    @Size(max = 2048)
    private String imageUrl;

    @URL
    @Size(max = 2048)
    private String thumbnailUrl;

    @Size(max = 255)
    private String altText;
}

