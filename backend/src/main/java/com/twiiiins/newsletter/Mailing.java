package com.twiiiins.newsletter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "newsletter_mailings") @Getter @Setter
public class Mailing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private Long newsId;
    private Instant createdAt = Instant.now();
    @Column(nullable = false, length = 255) private String titleEn;
    @Column(nullable = false, length = 255) private String titleDe;
    @Lob @Column(columnDefinition = "LONGTEXT") private String htmlEn;
    @Lob @Column(columnDefinition = "LONGTEXT") private String htmlDe;
    @Lob @Column(columnDefinition = "LONGTEXT") private String images;
    private boolean paused;
    private String pauseReason;
    private int acceptedCount;
    private int failedCount;
    private int skippedCount;
    private int unknownCount;
    private int totalCount;
}
