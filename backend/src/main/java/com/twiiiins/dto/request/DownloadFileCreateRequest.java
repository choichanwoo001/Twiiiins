package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import com.twiiiins.validation.ValidMediaUrl;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadFileCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @ValidMediaUrl
    @Size(max = 2048)
    private String fileUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


