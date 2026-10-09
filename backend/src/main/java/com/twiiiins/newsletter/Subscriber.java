package com.twiiiins.newsletter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity @Table(name = "newsletter_subscribers") @Getter @Setter
public class Subscriber {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 254) private String email;
    @Column(nullable = false, length = 2) private String language = "en";
    @Column(length = 2) private String pendingLanguage;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    @JsonIgnore @Column(length = 64) private String confirmationHash;
    @JsonIgnore private String confirmationContext;
    private Instant confirmationExpires;
    private Instant requestedAt;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    @Column(nullable = false) private Instant updatedAt = Instant.now();
    private Instant consentAt;
    private String consentVersion;
    @JsonIgnore private String unsubscribeVersion = java.util.UUID.randomUUID().toString();
}
