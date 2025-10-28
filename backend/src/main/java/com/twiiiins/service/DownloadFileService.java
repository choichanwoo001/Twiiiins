package com.twiiiins.service;

import com.twiiiins.dto.DownloadFileDto;
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
    
    public List<DownloadFileDto> getAllDownloadFiles() {
        return downloadFileRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public DownloadFileDto getDownloadFileById(Long id) {
        DownloadFile downloadFile = downloadFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadFile not found with id: " + id));
        return convertToDto(downloadFile);
    }
    
    public DownloadFileDto createDownloadFile(DownloadFileDto downloadFileDto) {
        DownloadFile downloadFile = convertToEntity(downloadFileDto);
        DownloadFile savedDownloadFile = downloadFileRepository.save(downloadFile);
        return convertToDto(savedDownloadFile);
    }
    
    public DownloadFileDto updateDownloadFile(Long id, DownloadFileDto downloadFileDto) {
        DownloadFile downloadFile = downloadFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadFile not found with id: " + id));
        
        downloadFile.setName(downloadFileDto.getName());
        downloadFile.setFileUrl(downloadFileDto.getFileUrl());
        downloadFile.setDisplayOrder(downloadFileDto.getDisplayOrder());
        
        DownloadFile savedDownloadFile = downloadFileRepository.save(downloadFile);
        return convertToDto(savedDownloadFile);
    }
    
    public void deleteDownloadFile(Long id) {
        downloadFileRepository.deleteById(id);
    }
    
    private DownloadFileDto convertToDto(DownloadFile downloadFile) {
        return new DownloadFileDto(
            downloadFile.getId(),
            downloadFile.getName(),
            downloadFile.getFileUrl(),
            downloadFile.getDisplayOrder()
        );
    }
    
    private DownloadFile convertToEntity(DownloadFileDto downloadFileDto) {
        DownloadFile downloadFile = new DownloadFile();
        downloadFile.setName(downloadFileDto.getName());
        downloadFile.setFileUrl(downloadFileDto.getFileUrl());
        downloadFile.setDisplayOrder(downloadFileDto.getDisplayOrder());
        return downloadFile;
    }
}
