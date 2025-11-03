package com.twiiiins.repository;

import com.twiiiins.entity.PhotoGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoGroupRepository extends JpaRepository<PhotoGroup, Long> {
    List<PhotoGroup> findAllByOrderByDisplayOrderAsc();
    
    @Query("SELECT pg FROM PhotoGroup pg WHERE " +
           "(:title IS NULL OR pg.title LIKE %:title%) " +
           "ORDER BY pg.displayOrder ASC")
    List<PhotoGroup> findPhotoGroupsWithFilters(
        @Param("title") String title
    );
}

