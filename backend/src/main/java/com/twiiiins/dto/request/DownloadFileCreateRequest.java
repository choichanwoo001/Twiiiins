package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class DownloadFileCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @URL
    @Size(max = 2048)
    private String fileUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


