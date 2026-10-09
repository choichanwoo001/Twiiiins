package com.twiiiins.newsletter;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
public interface MailGateRepository extends JpaRepository<MailGate, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from MailGate g where g.id = 1")
    MailGate lock();
}
