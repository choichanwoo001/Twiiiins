package com.twiiiins.newsletter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity @Table(name = "newsletter_deliveries", uniqueConstraints = @UniqueConstraint(columnNames = {"mailing_id", "subscriber_id"}),
    indexes = {@Index(columnList = "status,nextAttempt"), @Index(columnList = "attemptedAt")}) @Getter @Setter
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "mailing_id") private Long mailingId;
    @Column(name = "subscriber_id") private Long subscriberId;
    @Column(nullable = false, length = 20) private String kind; // WELCOME, legacy CONFIRM, TEST, NEWS
    @Column(length = 254) private String email;
    @Column(length = 2) private String language;
    @Column(nullable = false, length = 20) private String status = "WAITING";
    @JsonIgnore private String confirmationContext;
    @JsonIgnore private String subscriptionVersion;
    @Lob @Column(columnDefinition = "LONGTEXT") @JsonIgnore private String testHtml;
    @JsonIgnore private String testTitle;
    private int attempts;
    private String errorCode;
    private Instant createdAt = Instant.now();
    private Instant nextAttempt = Instant.now();
    private Instant attemptedAt;
    private Instant finishedAt;
}
