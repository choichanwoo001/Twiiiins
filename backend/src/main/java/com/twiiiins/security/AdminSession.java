package com.twiiiins.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Table(name = "admin_sessions") @Getter @Setter
public class AdminSession {
    @Id private String tokenHash;
    @Column(nullable = false) private String username;
    @Column(nullable = false) private Instant expiresAt;
}
