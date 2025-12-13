package com.twiiiins.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

@Getter
@Setter
public class ConcertUpdateRequest {

    @NotNull
    private LocalDate date;

    @NotBlank
    @Size(min = 1, max = 255)
    private String location;

    @NotBlank
    @Size(min = 1, max = 255)
    private String name;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "시작 시간은 HH:mm 형식이어야 합니다.")
    private String startTime;

    @Size(max = 255)
    private String ticketInfo;

    @Size(max = 255)
    private String fullLocation;

    @URL
    @Size(max = 2048)
    private String googleMapUrl;

    @Size(max = 500)
    private String collaborationInfo;

    private Boolean isPast;
}
