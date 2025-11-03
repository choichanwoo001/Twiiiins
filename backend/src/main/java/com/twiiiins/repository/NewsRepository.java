package com.twiiiins.repository;

import com.twiiiins.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllByOrderByDisplayOrderAsc();
    List<News> findAllByOrderByDateDesc();
    
    @Query("SELECT n FROM News n WHERE " +
           "(:title IS NULL OR n.title LIKE %:title%) AND " +
           "(:startDate IS NULL OR n.date >= :startDate) AND " +
           "(:endDate IS NULL OR n.date <= :endDate) " +
           "ORDER BY n.displayOrder ASC")
    List<News> findNewsWithFilters(
        @Param("title") String title,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

