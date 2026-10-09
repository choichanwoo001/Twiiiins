package com.twiiiins.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "news")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {
    public static final String NEWS = "NEWS";
    public static final String NEWSLETTER = "NEWSLETTER";

    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'NEWS'")
    private String source = NEWS;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 255) private String eventWhenEn;
    @Column(length = 255) private String eventWhenDe;
    @Column(length = 255) private String eventLocationEn;
    @Column(length = 255) private String eventLocationDe;
    @Column(length = 255) private String ctaLabelEn;
    @Column(length = 255) private String ctaLabelDe;
    @Column(length = 2048) private String ctaUrl;
    private String titleDe;
    @Lob @Column(columnDefinition = "LONGTEXT") private String bodyEn;
    @Lob @Column(columnDefinition = "LONGTEXT") private String bodyDe;
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PUBLISHED'")
    private String status = "DRAFT";
    private boolean archived;
    @Version private Long version;
    @ElementCollection
    @CollectionTable(name = "news_videos", joinColumns = @JoinColumn(name = "news_id"))
    @OrderColumn(name = "video_order")
    @Column(name = "video_url", length = 2048)
    private List<String> videoUrls = new ArrayList<>();

    @Column(name = "display_order")
    private Integer displayOrder;
    
    @ElementCollection
    @CollectionTable(name = "news_images", joinColumns = @JoinColumn(name = "news_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_url", length = 2048)
    private List<String> imageUrls = new ArrayList<>();
}

