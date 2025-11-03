package com.twiiiins.repository;

import com.twiiiins.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findAllByOrderByDisplayOrderAsc();
    
    @Query("SELECT v FROM Video v WHERE " +
           "(:title IS NULL OR v.title LIKE %:title%) " +
           "ORDER BY v.displayOrder ASC")
    List<Video> findVideosWithFilters(
        @Param("title") String title
    );
}

