package com.twiiiins.repository;

import com.twiiiins.entity.PhotoGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoGroupRepository extends JpaRepository<PhotoGroup, Long> {
    List<PhotoGroup> findAllByOrderByDisplayOrderAsc();
}

