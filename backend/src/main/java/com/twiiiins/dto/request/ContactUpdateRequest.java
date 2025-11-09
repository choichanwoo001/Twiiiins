package com.twiiiins.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactUpdateRequest {

    @Size(min = 1, max = 100)
    private String name;

    @Size(min = 1, max = 100)
    private String role;

    @Email
    @Size(min = 1, max = 255)
    private String email;

    @PositiveOrZero
    private Integer displayOrder;
}


