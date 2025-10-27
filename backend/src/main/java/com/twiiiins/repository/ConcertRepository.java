package com.twiiiins.repository;

import com.twiiiins.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, Long> {
    List<Concert> findByIsPastOrderByDateAsc(Boolean isPast);
    List<Concert> findAllByOrderByDateDesc();
    List<Concert> findByDateBeforeAndIsPast(LocalDate date, Boolean isPast);
}

