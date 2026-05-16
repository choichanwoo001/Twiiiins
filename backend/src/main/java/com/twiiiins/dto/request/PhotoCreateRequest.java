package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.twiiiins.validation.ValidMediaUrl;

@Getter
@Setter
public class PhotoCreateRequest {

    @NotBlank
    @ValidMediaUrl
    @Size(max = 2048)
    private String imageUrl;

    @ValidMediaUrl
    @Size(max = 2048)
    private String thumbnailUrl;

    @Size(max = 255)
    private String altText;
}

