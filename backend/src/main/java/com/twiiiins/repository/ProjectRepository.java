package com.twiiiins.repository;

import com.twiiiins.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOrderByDisplayOrderAsc();
    Optional<Project> findByUrlSlug(String urlSlug);
    
    @Query("SELECT p FROM Project p WHERE " +
           "(:title IS NULL OR p.title LIKE %:title%) AND " +
           "(:location IS NULL OR p.location LIKE %:location%) AND " +
           "(:startDate IS NULL OR p.premiereDate >= :startDate) AND " +
           "(:endDate IS NULL OR p.premiereDate <= :endDate) " +
           "ORDER BY p.displayOrder ASC")
    List<Project> findProjectsWithFilters(
        @Param("title") String title,
        @Param("location") String location,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

