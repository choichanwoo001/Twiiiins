package com.twiiiins.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "concerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Concert {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)   
    private String location;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "start_time")
    private String startTime;
    
    @Column(name = "ticket_info")
    private String ticketInfo;
    
    @Column(name = "full_location")
    private String fullLocation;
    
    @Column(name = "google_map_url")
    private String googleMapUrl;
    
    @Column(name = "collaboration_info")
    private String collaborationInfo;
    
    @Column(name = "is_past", nullable = false)
    @JsonProperty("isPast")
    private Boolean isPast = false;
}

