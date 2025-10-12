package com.twiiiins.repository;

import com.twiiiins.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {
    
    List<Media> findByPublishedTrueOrderByCreatedAtDesc();
    
    List<Media> findByTypeAndPublishedTrueOrderByCreatedAtDesc(Media.MediaType type);
    
    List<Media> findByCategoryAndPublishedTrueOrderByCreatedAtDesc(String category);
}

