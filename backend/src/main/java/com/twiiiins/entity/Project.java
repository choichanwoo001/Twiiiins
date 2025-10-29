package com.twiiiins.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    private String subtitle;
    
    @Column(name = "premiere_date")
    private LocalDate premiereDate;
    
    private String location;
    
    @Column(length = 2000)
    private String description1;
    
    @Column(length = 2000)
    private String description2;
    
    @Column(length = 2000)
    private String description3;
    
    @Column(name = "main_image_url")
    private String mainImageUrl;
    
    @Column(name = "horizontal1_image_url")
    private String horizontal1ImageUrl;
    
    @Column(name = "horizontal2_image_url")
    private String horizontal2ImageUrl;
    
    @Column(name = "vertical1_image_url")
    private String vertical1ImageUrl;
    
    @Column(name = "vertical2_image_url")
    private String vertical2ImageUrl;
    
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    
    @Column(name = "more_info_url")
    private String moreInfoUrl;
    
    @Column(length = 1000)
    private String review1Text;
    
    private String review1Source;
    
    @Column(length = 1000)
    private String review2Text;
    
    private String review2Source;
    
    @Column(name = "url_slug")
    private String urlSlug;
    
    @Column(name = "display_order")
    private Integer displayOrder;
}

