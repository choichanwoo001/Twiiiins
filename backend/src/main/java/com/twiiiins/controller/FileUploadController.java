package com.twiiiins.controller;

import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    @PostMapping("/image")
    public ResponseEntity<FileUploadResponseDto> uploadImage(@RequestParam("file") MultipartFile file) {
        FileUploadResponseDto response = fileUploadService.uploadImage(file);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/file")
    public ResponseEntity<FileUploadResponseDto> uploadFile(@RequestParam("file") MultipartFile file) {
        FileUploadResponseDto response = fileUploadService.uploadFile(file);
        return ResponseEntity.ok(response);
    }
}
