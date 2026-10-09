package com.twiiiins.newsletter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
@Entity @Table(name = "newsletter_attempts", indexes = @Index(columnList = "attemptedAt")) @Getter @Setter
public class MailAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long deliveryId;
    private Instant attemptedAt = Instant.now();
}
