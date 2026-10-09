package com.twiiiins.newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
public interface MailAttemptRepository extends JpaRepository<MailAttempt, Long> {
    long countByAttemptedAtGreaterThanEqual(Instant since);
    void deleteByAttemptedAtBefore(Instant before);
}
