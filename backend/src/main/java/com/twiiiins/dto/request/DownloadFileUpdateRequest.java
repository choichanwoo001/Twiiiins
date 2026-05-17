package com.twiiiins.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import com.twiiiins.validation.ValidMediaUrl;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DownloadFileUpdateRequest {

    @Size(min = 1, max = 255)
    private String name;

    @ValidMediaUrl
    @Size(max = 2048)
    private String fileUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


