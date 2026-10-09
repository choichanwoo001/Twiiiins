package com.twiiiins.newsletter;

import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Subscriber s where s.id = :id")
    Optional<Subscriber> lockById(@org.springframework.data.repository.query.Param("id") Long id);
    Optional<Subscriber> findByConfirmationHash(String hash);
    List<Subscriber> findByStatus(String status);
}
