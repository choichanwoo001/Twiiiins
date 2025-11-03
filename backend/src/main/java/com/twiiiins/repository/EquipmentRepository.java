package com.twiiiins.repository;

import com.twiiiins.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findAllByOrderByDisplayOrderAsc();
    
    @Query("SELECT e FROM Equipment e WHERE " +
           "(:name IS NULL OR e.name LIKE %:name%) " +
           "ORDER BY e.displayOrder ASC")
    List<Equipment> findEquipmentWithFilters(
        @Param("name") String name
    );
}

