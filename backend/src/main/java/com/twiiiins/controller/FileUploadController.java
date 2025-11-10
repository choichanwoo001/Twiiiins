package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.service.FileUploadService;
import com.twiiiins.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        Map<String, Object> data = fileUploadService.uploadImages(files);
        int successCount = ((List<?>) data.getOrDefault("urls", List.of())).size();
        return ResponseUtil.success(data, String.format("%d개 파일 업로드 완료", successCount));
    }
    
    @PostMapping("/file")
    public ResponseEntity<FileUploadResponseDto> uploadFile(@RequestParam("file") @NonNull MultipartFile file) {
        Assert.notNull(file, "file must not be null");
        FileUploadResponseDto response = fileUploadService.uploadFile(file);
        return ResponseEntity.ok(response);
    }
}
