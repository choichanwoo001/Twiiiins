package com.twiiiins.translation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "content_translations", uniqueConstraints = @UniqueConstraint(columnNames = {"kind", "content_id", "field_path"}),
    indexes = @Index(name = "idx_translation_work", columnList = "state,next_attempt"))
@Getter @Setter
public class ContentTranslation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 32) private String kind;
    @Column(name = "content_id", nullable = false) private Long contentId;
    @Column(name = "field_path", nullable = false, length = 128) private String fieldPath;
    @Column(nullable = false, length = 64) private String sourceHash;
    @Lob @Column(columnDefinition = "LONGTEXT") private String sourceText;
    @Column(length = 2) private String sourceLanguage;
    @Lob @Column(columnDefinition = "LONGTEXT") private String textEn;
    @Lob @Column(columnDefinition = "LONGTEXT") private String textDe;
    private boolean manualEn;
    private boolean manualDe;
    @Column(nullable = false, length = 16) private String state;
    private int attempts;
    @Column(name = "next_attempt", nullable = false) private Instant nextAttempt = Instant.EPOCH;
    @Column(length = 40) private String errorCode;
    @Version private Long version;
}
