package com.twiiiins.service;

import com.twiiiins.entity.DownloadFile;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.DownloadFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DownloadFileService {
    
    private final DownloadFileRepository downloadFileRepository;
    
    public List<DownloadFile> getAllDownloadFiles() {
        return downloadFileRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public DownloadFile getDownloadFileById(Long id) {
        return downloadFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadFile not found with id: " + id));
    }
    
    public DownloadFile createDownloadFile(DownloadFile downloadFile) {
        return downloadFileRepository.save(downloadFile);
    }
    
    public DownloadFile updateDownloadFile(Long id, DownloadFile downloadFileDetails) {
        DownloadFile downloadFile = getDownloadFileById(id);
        downloadFile.setName(downloadFileDetails.getName());
        downloadFile.setFileUrl(downloadFileDetails.getFileUrl());
        downloadFile.setDisplayOrder(downloadFileDetails.getDisplayOrder());
        return downloadFileRepository.save(downloadFile);
    }
    
    public void deleteDownloadFile(Long id) {
        downloadFileRepository.deleteById(id);
    }
}
