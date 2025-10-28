package com.twiiiins.service;

import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadService {
    
    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "application/pdf"
    );
    
    public FileUploadResponseDto uploadImage(MultipartFile file) {
        validateFile(file, ALLOWED_IMAGE_TYPES, "이미지 파일만 업로드 가능합니다.");
        return uploadFile(file, "image");
    }
    
    public FileUploadResponseDto uploadFile(MultipartFile file) {
        validateFile(file, ALLOWED_FILE_TYPES, "이미지 파일 또는 PDF 파일만 업로드 가능합니다.");
        return uploadFile(file, "file");
    }
    
    private void validateFile(MultipartFile file, List<String> allowedTypes, String errorMessage) {
        if (file.isEmpty()) {
            throw new FileUploadException("파일이 비어있습니다.");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("파일 크기는 15MB를 초과할 수 없습니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new FileUploadException(errorMessage);
        }
    }
    
    private FileUploadResponseDto uploadFile(MultipartFile file, String uploadType) {
        try {
            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + extension;
            
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 파일 저장
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 접근 가능한 URL 반환
            String fileUrl = "/uploads/" + filename;
            
            log.info("파일 업로드 성공: {} -> {}", originalFilename, filename);
            
            return new FileUploadResponseDto(
                fileUrl, 
                filename, 
                originalFilename, 
                file.getSize(), 
                file.getContentType()
            );
            
        } catch (IOException e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            throw new FileUploadException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
}
