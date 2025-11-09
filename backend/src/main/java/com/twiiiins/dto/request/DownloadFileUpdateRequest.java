package com.twiiiins.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class DownloadFileUpdateRequest {

    @Size(min = 1, max = 255)
    private String name;

    @URL
    @Size(max = 2048)
    private String fileUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


