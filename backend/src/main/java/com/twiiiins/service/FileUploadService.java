package com.twiiiins.service;

import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {
    
    private final S3FileService s3FileService;
    
    private static final String UPLOAD_DIR = "uploads/";
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15MB
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "application/pdf"
    );
    
    public FileUploadResponseDto uploadImage(MultipartFile file) {
        log.info("이미지 파일 업로드 시작: 원본 파일명 = {}, 크기 = {} bytes", 
                file.getOriginalFilename(), file.getSize());
        validateFile(file, ALLOWED_IMAGE_TYPES, "이미지 파일만 업로드 가능합니다.");
        return uploadFile(file, "image");
    }
    
    public FileUploadResponseDto uploadFile(MultipartFile file) {
        log.info("파일 업로드 시작: 원본 파일명 = {}, 크기 = {} bytes, MIME 타입 = {}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        validateFile(file, ALLOWED_FILE_TYPES, "이미지 파일 또는 PDF 파일만 업로드 가능합니다.");
        return uploadFile(file, "file");
    }
    
    private void validateFile(MultipartFile file, List<String> allowedTypes, String errorMessage) {
        log.debug("파일 검증 시작: 파일명 = {}, 크기 = {} bytes", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            log.warn("빈 파일 업로드 시도: {}", file.getOriginalFilename());
            throw new FileUploadException("파일이 비어있습니다.");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기 초과: {} bytes (최대: {} bytes)", file.getSize(), MAX_FILE_SIZE);
            throw new FileUploadException("파일 크기는 15MB를 초과할 수 없습니다.");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            log.warn("허용되지 않은 파일 타입: {} (허용 타입: {})", contentType, allowedTypes);
            throw new FileUploadException(errorMessage);
        }
        
        log.debug("파일 검증 완료: 파일명 = {}, 타입 = {}", file.getOriginalFilename(), contentType);
    }
    
    private FileUploadResponseDto uploadFile(MultipartFile file, String uploadType) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;
        String fileUrl = null;
        
        try {
            // S3에 파일 업로드 시도
            fileUrl = s3FileService.uploadFile(file, uploadType);
            
            // S3 업로드 성공
            if (fileUrl != null && !fileUrl.isEmpty()) {
                log.info("파일이 S3에 업로드되었습니다: {} -> {}", originalFilename, fileUrl);
                return new FileUploadResponseDto(
                    fileUrl, 
                    filename, 
                    originalFilename, 
                    file.getSize(), 
                    file.getContentType()
                );
            }
        } catch (Exception s3Error) {
            log.warn("S3 업로드 시도 중 오류 (로컬로 fallback): {}", s3Error.getMessage());
        }
        
        // S3 업로드 실패 시 로컬 저장소 사용
        try {
            log.warn("S3 업로드 실패, 로컬 저장소 사용: {}", originalFilename);
            
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 파일 저장 (inputStream은 S3 업로드에서 이미 소비되었을 수 있으므로 getBytes 사용)
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, file.getBytes());
            
            // 접근 가능한 URL 반환 (상대 경로)
            fileUrl = "/uploads/" + filename;
            
            log.info("파일 업로드 성공 (로컬): {} -> {}", originalFilename, filename);
            
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
