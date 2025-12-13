package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class NewsUpdateRequest {

    @NotNull
    private LocalDate date;

    @NotBlank
    @Size(min = 1, max = 255)
    private String title;

    @Size(max = 1000)
    private String description;

    @PositiveOrZero
    private Integer displayOrder;

    @Size(max = 50)
    private List<@NotBlank @Size(max = 2048) @URL String> imageUrls;
}
