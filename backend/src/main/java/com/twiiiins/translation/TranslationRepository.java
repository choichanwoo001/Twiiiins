package com.twiiiins.translation;

import java.time.Instant;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TranslationRepository extends JpaRepository<ContentTranslation, Long> {
    List<ContentTranslation> findByKindAndContentId(String kind, Long contentId);
    List<ContentTranslation> findByKindAndContentIdIn(String kind, Collection<Long> ids);
    @Query("select t from ContentTranslation t where (:kind is null or t.kind = :kind) and (:contentId is null or t.contentId = :contentId)")
    org.springframework.data.domain.Page<ContentTranslation> search(@Param("kind") String kind, @Param("contentId") Long contentId, org.springframework.data.domain.Pageable page);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from ContentTranslation t where t.id = :id")
    Optional<ContentTranslation> lock(@Param("id") Long id);
    @Query("select t.id from ContentTranslation t where t.state in ('PENDING','PROCESSING','DISABLED') and t.nextAttempt <= :now order by t.nextAttempt, t.id")
    List<Long> due(@Param("now") Instant now, org.springframework.data.domain.Pageable page);
    void deleteByKindAndContentId(String kind, Long contentId);
}
