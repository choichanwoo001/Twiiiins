package com.twiiiins.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    
    @ElementCollection
    @CollectionTable(name = "project_descriptions", joinColumns = @JoinColumn(name = "project_id"))
    @OrderColumn(name = "description_order")
    @Column(name = "description", length = 2000)
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<String> descriptions = new ArrayList<>();
    
    @Column(name = "cover_image_url")
    private String coverImageUrl;
    
    @Column(name = "more_info_url")
    private String moreInfoUrl;
    
    private String director;
    
    @Column(name = "thank_you_text", length = 2000)
    private String thankYouText;
    
    @Column(name = "url_slug")
    private String urlSlug;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @ElementCollection
    @CollectionTable(name = "project_images", joinColumns = @JoinColumn(name = "project_id"))
    @OrderColumn(name = "image_order")
    @Column(name = "image_url")
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<String> imageUrls = new ArrayList<>();
    
    // 리뷰 배열 (새로운 방식)
    @ElementCollection
    @CollectionTable(name = "project_reviews", joinColumns = @JoinColumn(name = "project_id"))
    @OrderColumn(name = "review_order")
    @Fetch(FetchMode.SUBSELECT)
    @BatchSize(size = 20)
    private List<Review> reviews = new ArrayList<>();
    
    // Review 임베디드 클래스
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Review {
        @Column(length = 1000)
        private String text;
        
        @Column(length = 500)
        private String source;
    }
}

