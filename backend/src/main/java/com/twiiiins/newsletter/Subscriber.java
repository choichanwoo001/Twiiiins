package com.twiiiins.newsletter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity @Table(name = "newsletter_subscribers", uniqueConstraints = @UniqueConstraint(name = "newsletter_email_key", columnNames = "email_key")) @Getter @Setter
public class Subscriber {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 254) private String email;
    @JsonIgnore @Column(name = "email_key", length = 254) private String emailKey;
    @Column(nullable = false, length = 2) private String language = "en";
    @Column(length = 2) private String pendingLanguage;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    @JsonIgnore @Column(length = 20) private String statusBeforeExclusion;
    @JsonIgnore @Column(length = 64) private String confirmationHash;
    @JsonIgnore private String confirmationContext;
    private Instant confirmationExpires;
    private Instant requestedAt;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();
    private Instant consentAt;
    @JsonIgnore private Instant unsubscribedAt;
    private String consentVersion;
    @JsonIgnore private String unsubscribeVersion = java.util.UUID.randomUUID().toString();

    @PrePersist
    void assignEmailKey() { emailKey = NewsletterMailboxes.emailKey(email); }

    void clearConfirmation() {
        confirmationHash = null;
        confirmationContext = null;
        confirmationExpires = null;
        pendingLanguage = null;
    }
}
