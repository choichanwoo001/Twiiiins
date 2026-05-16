package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import com.twiiiins.validation.ValidMediaUrl;

@Getter
@Setter
public class EquipmentCreateRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @ValidMediaUrl
    @Size(max = 2048)
    private String imageUrl;

    @PositiveOrZero
    private Integer displayOrder;
}


