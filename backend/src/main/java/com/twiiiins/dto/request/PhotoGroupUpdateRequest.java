package com.twiiiins.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhotoGroupUpdateRequest {

    @Size(min = 1, max = 255)
    private String title;

    @PositiveOrZero
    private Integer displayOrder;
}

