package com.twiiiins.newsletter;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Delivery d where d.status = 'WAITING' and d.nextAttempt <= :now order by case when d.kind = 'CONFIRM' then 0 else 1 end, d.id")
    List<Delivery> next(@Param("now") Instant now, Pageable page);
    long countByAttemptedAtGreaterThanEqual(Instant since);
    List<Delivery> findByMailingIdOrderByIdAsc(Long id);
    List<Delivery> findByStatus(String status);
}
