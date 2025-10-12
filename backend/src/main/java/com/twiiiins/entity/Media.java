package com.twiiiins.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;

    @Column(nullable = false)
    private String url;  // Cloudflare Stream/Images URL

    private String thumbnailUrl;  // 썸네일 URL

    private String category;  // 갤러리 카테고리

    private String tags;  // 태그 (콤마 구분)

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum MediaType {
        VIDEO,      // Cloudflare Stream
        IMAGE       // Cloudflare Images
    }
}

