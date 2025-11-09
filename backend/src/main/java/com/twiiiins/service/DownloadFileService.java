package com.twiiiins.service;

import com.twiiiins.dto.DownloadFileDto;
import com.twiiiins.dto.request.DownloadFileCreateRequest;
import com.twiiiins.dto.request.DownloadFileUpdateRequest;
import com.twiiiins.entity.DownloadFile;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.DownloadFileMapper;
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
    private final DownloadFileMapper downloadFileMapper;
    
    public List<DownloadFileDto> getAllDownloadFiles() {
        return downloadFileRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(downloadFileMapper::toDto)
                .toList();
    }
    
    public DownloadFileDto getDownloadFileById(Long id) {
        DownloadFile downloadFile = downloadFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadFile not found with id: " + id));
        return downloadFileMapper.toDto(downloadFile);
    }
    
    public DownloadFileDto createDownloadFile(DownloadFileCreateRequest request) {
        DownloadFile downloadFile = downloadFileMapper.toEntity(request);
        DownloadFile savedDownloadFile = downloadFileRepository.save(downloadFile);
        return downloadFileMapper.toDto(savedDownloadFile);
    }
    
    public DownloadFileDto updateDownloadFile(Long id, DownloadFileUpdateRequest request) {
        DownloadFile downloadFile = downloadFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadFile not found with id: " + id));
        
        downloadFileMapper.updateEntityFromUpdateRequest(request, downloadFile);
        
        DownloadFile savedDownloadFile = downloadFileRepository.save(downloadFile);
        return downloadFileMapper.toDto(savedDownloadFile);
    }
    
    public void deleteDownloadFile(Long id) {
        downloadFileRepository.deleteById(id);
    }
}
