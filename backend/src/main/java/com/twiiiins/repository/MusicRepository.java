package com.twiiiins.repository;

import com.twiiiins.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicRepository extends JpaRepository<Music, Long> {
    List<Music> findAllByOrderByDisplayOrderAsc();
    
    @Query("SELECT m FROM Music m WHERE " +
           "(:title IS NULL OR m.title LIKE %:title%) AND " +
           "(:artist IS NULL OR m.artist LIKE %:artist%) " +
           "ORDER BY m.displayOrder ASC")
    List<Music> findMusicWithFilters(
        @Param("title") String title,
        @Param("artist") String artist
    );
}

