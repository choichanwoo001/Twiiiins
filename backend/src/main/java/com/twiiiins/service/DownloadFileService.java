package com.twiiiins.service;

import com.twiiiins.dto.DownloadFileDto;
import com.twiiiins.dto.request.DownloadFileCreateRequest;
import com.twiiiins.dto.request.DownloadFileUpdateRequest;
import com.twiiiins.entity.DownloadFile;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.DownloadFileMapper;
import com.twiiiins.repository.DownloadFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
    
    public DownloadFileDto getDownloadFileById(@NonNull Long id) {
        DownloadFile downloadFile = downloadFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadFile not found with id: " + id));
        return downloadFileMapper.toDto(downloadFile);
    }
    
    public DownloadFileDto createDownloadFile(@NonNull DownloadFileCreateRequest request) {
        DownloadFile downloadFile = Objects.requireNonNull(
                downloadFileMapper.toEntity(request),
                "DownloadFileMapper.toEntity returned null"
        );
        DownloadFile savedDownloadFile = downloadFileRepository.save(
                Objects.requireNonNull(downloadFile, "DownloadFile must not be null"));
        return downloadFileMapper.toDto(savedDownloadFile);
    }
    
    public DownloadFileDto updateDownloadFile(@NonNull Long id, @NonNull DownloadFileUpdateRequest request) {
        DownloadFile downloadFile = downloadFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DownloadFile not found with id: " + id));
        
        downloadFileMapper.updateEntityFromUpdateRequest(request, downloadFile);
        
        DownloadFile savedDownloadFile = downloadFileRepository.save(
                Objects.requireNonNull(downloadFile, "DownloadFile must not be null"));
        return downloadFileMapper.toDto(savedDownloadFile);
    }
    
    public void deleteDownloadFile(@NonNull Long id) {
        downloadFileRepository.deleteById(id);
    }
}
