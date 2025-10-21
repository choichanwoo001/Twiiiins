package com.twiiiins.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @Column(name = "alt_text")
    private String altText;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_group_id", nullable = false)
    private PhotoGroup photoGroup;
}

