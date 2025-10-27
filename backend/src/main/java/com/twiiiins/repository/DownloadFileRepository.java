package com.twiiiins.repository;

import com.twiiiins.entity.DownloadFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DownloadFileRepository extends JpaRepository<DownloadFile, Long> {
    List<DownloadFile> findAllByOrderByDisplayOrderAsc();
}
