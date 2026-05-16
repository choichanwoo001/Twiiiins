package com.twiiiins.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.twiiiins.validation.ValidMediaUrl;

@Getter
@Setter
public class PhotoUpdateRequest {

    @ValidMediaUrl
    @Size(max = 2048)
    private String imageUrl;

    @ValidMediaUrl
    @Size(max = 2048)
    private String thumbnailUrl;

    @Size(min = 1, max = 255)
    private String altText;
}

