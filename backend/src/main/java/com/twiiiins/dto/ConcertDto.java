package com.twiiiins.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcertDto {
    private Long id;
    private LocalDate date;
    private String location;
    private String name;
    private String startTime;
    private String ticketInfo;
    private String fullLocation;
    private String googleMapUrl;
    private String collaborationInfo;
    private Boolean isPast;
}
