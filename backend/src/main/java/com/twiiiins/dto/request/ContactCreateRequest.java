package com.twiiiins.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String role;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @PositiveOrZero
    private Integer displayOrder;
}


