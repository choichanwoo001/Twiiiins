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
    List<Delivery> findBySubscriberIdAndStatus(Long subscriberId, String status);
    @Query("""
        select count(d) from Delivery d
        where d.mailingId = :mailingId and d.id <> :deliveryId
          and d.status in ('ACCEPTED', 'SENDING', 'UNKNOWN')
          and lower(d.email) like '%@gmail.com'
          and replace(lower(d.email), '.', '') = :mailbox
        """)
    long countOtherSentToGmail(@Param("mailingId") Long mailingId, @Param("deliveryId") Long deliveryId,
                               @Param("mailbox") String mailbox);
}
