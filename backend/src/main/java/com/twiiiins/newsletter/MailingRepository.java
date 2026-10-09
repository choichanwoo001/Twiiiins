package com.twiiiins.newsletter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface MailingRepository extends JpaRepository<Mailing, Long> {
    Optional<Mailing> findByNewsId(Long newsId);
    boolean existsByNewsId(Long newsId);
}
