package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @PostMapping("/image")
    public ResponseEntity<FileUploadResponseDto> uploadImage(@RequestParam("file") @NonNull MultipartFile file) {
        Assert.notNull(file, "file must not be null");
        FileUploadResponseDto response = fileUploadService.uploadImage(file);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/images")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        MultipartFile[] safeFiles = files != null ? files : new MultipartFile[0];
        log.info("다중 이미지 업로드 시작: {} 개 파일", safeFiles.length);
        List<String> urls = new ArrayList<>();
        List<String> filenames = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (MultipartFile file : safeFiles) {
            if (file == null) {
                errors.add("null 파일은 업로드할 수 없습니다.");
                continue;
            }
            try {
                FileUploadResponseDto response = fileUploadService.uploadImage(file);
                urls.add(response.getUrl());
                filenames.add(response.getFilename());
                log.debug("파일 업로드 성공: {}", file.getOriginalFilename());
            } catch (Exception e) {
                log.warn("파일 업로드 실패: {} - {}", file.getOriginalFilename(), e.getMessage());
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("urls", urls);
        data.put("filenames", filenames);
        if (!errors.isEmpty()) {
            data.put("errors", errors);
        }
        
        log.info("다중 이미지 업로드 완료: 성공 {} 개, 실패 {} 개", urls.size(), errors.size());
        return ResponseEntity.ok(ApiResponse.success(data, String.format("%d개 파일 업로드 완료", urls.size())));
    }
    
    @PostMapping("/file")
    public ResponseEntity<FileUploadResponseDto> uploadFile(@RequestParam("file") @NonNull MultipartFile file) {
        Assert.notNull(file, "file must not be null");
        FileUploadResponseDto response = fileUploadService.uploadFile(file);
        return ResponseEntity.ok(response);
    }
}
