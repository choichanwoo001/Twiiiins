package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class EquipmentCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @URL
    @Size(max = 2048)
    private String imageUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


