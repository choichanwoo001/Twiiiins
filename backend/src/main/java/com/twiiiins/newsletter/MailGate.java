package com.twiiiins.newsletter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity @Table(name = "newsletter_gate") @Getter @Setter
public class MailGate {
    @Id private Long id = 1L;
    private boolean paused;
    private String reason;
}
