package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhotoGroupCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @PositiveOrZero
    private Integer displayOrder;
}

