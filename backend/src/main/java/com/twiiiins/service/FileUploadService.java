package com.twiiiins.service;

import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {
    
    private final S3FileService s3FileService;
    private final ImageResizeService imageResizeService;
    
    @Value("${UPLOAD_MAX_SIZE:15MB}")
    private String uploadMaxSize;
    
    private long getMaxFileSize() {
        try {
            return DataSize.parse(uploadMaxSize).toBytes();
        } catch (Exception e) {
            log.warn("UPLOAD_MAX_SIZE 파싱 실패, 기본값 15MB 사용: {}", e.getMessage());
            return 15 * 1024 * 1024; // 기본값 15MB
        }
    }
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
    
    /**
     * 이미지와 썸네일을 함께 업로드
     * @param file 원본 이미지 파일
     * @return 원본 URL과 썸네일 URL을 포함한 응답 (thumbnailUrl 필드 추가 필요 시 별도 DTO 사용)
     */
    public FileUploadResponseDto uploadImageWithThumbnail(MultipartFile file) {
        log.info("이미지 및 썸네일 업로드 시작: 원본 파일명 = {}, 크기 = {} bytes", 
                file.getOriginalFilename(), file.getSize());
        validateFile(file, ALLOWED_IMAGE_TYPES, "이미지 파일만 업로드 가능합니다.");
        return uploadFileWithThumbnail(file, "image");
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
        
        long maxFileSize = getMaxFileSize();
        if (file.getSize() > maxFileSize) {
            log.warn("파일 크기 초과: {} bytes (최대: {} bytes)", file.getSize(), maxFileSize);
            throw new FileUploadException(String.format("파일 크기는 %s를 초과할 수 없습니다.", uploadMaxSize));
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
                    null, // thumbnailUrl은 이미지만 지원
                    filename, 
                    originalFilename, 
                    file.getSize(), 
                    file.getContentType()
                );
            }
        } catch (Exception s3Error) {
            log.warn("S3 업로드 시도 중 오류 (로컬로 fallback): {}", s3Error.getMessage());
        }
        
        // S3 업로드 실패 시 로컬 저장소 사용 (하지만 S3가 필수이므로 실패 시 에러)
        log.error("S3 업로드 실패: {} - S3 업로드는 필수입니다. 버킷 정책을 확인해주세요.", originalFilename);
        throw new FileUploadException("S3 업로드에 실패했습니다. AWS 설정을 확인해주세요: " + 
                (fileUrl != null ? "ACL이 허용되지 않는 버킷입니다. 버킷 정책으로 공개 접근을 허용해야 합니다." : "S3 업로드 중 오류가 발생했습니다."));
    }
    
    /**
     * 이미지와 썸네일을 함께 업로드하는 메서드
     */
    private FileUploadResponseDto uploadFileWithThumbnail(MultipartFile file, String uploadType) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;
        String fileUrl = null;
        String thumbnailUrl = null;
        
        try {
            // 원본 이미지를 S3에 업로드
            fileUrl = s3FileService.uploadFile(file, uploadType);
            
            if (fileUrl == null || fileUrl.isEmpty()) {
                throw new FileUploadException("원본 이미지 업로드 실패");
            }
            
            log.info("원본 이미지가 S3에 업로드되었습니다: {} -> {}", originalFilename, fileUrl);
            
            // 썸네일 생성 및 업로드 (150x150, 품질 0.85)
            try {
                byte[] thumbnailBytes = imageResizeService.generateThumbnail(file, 150, 150, 0.85f);
                
                // 썸네일을 MultipartFile로 변환
                MultipartFile thumbnailFile = new ThumbnailMultipartFile(
                    "thumb_" + filename, 
                    "thumb_" + originalFilename,
                    file.getContentType(),
                    thumbnailBytes
                );
                
                // 썸네일을 S3에 업로드 (thumbnails 폴더)
                thumbnailUrl = s3FileService.uploadFile(thumbnailFile, uploadType + "/thumbnails");
                
                if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
                    log.warn("썸네일 업로드 실패 (원본은 업로드됨): {}", fileUrl);
                } else {
                    log.info("썸네일이 S3에 업로드되었습니다: {}", thumbnailUrl);
                }
            } catch (Exception thumbnailError) {
                log.warn("썸네일 생성/업로드 실패 (원본은 업로드됨): {}", thumbnailError.getMessage());
                // 썸네일 생성 실패해도 원본은 업로드되므로 계속 진행
            }
            
            return new FileUploadResponseDto(
                fileUrl,
                thumbnailUrl,
                filename,
                originalFilename,
                file.getSize(),
                file.getContentType()
            );
            
        } catch (FileUploadException e) {
            throw e;
        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new FileUploadException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    /**
     * 썸네일을 MultipartFile로 변환하기 위한 래퍼 클래스
     */
    private static class ThumbnailMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;
        
        public ThumbnailMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }
        
        @Override
        public String getName() { 
            return name != null ? name : ""; 
        }
        
        @Override
        public String getOriginalFilename() { 
            return originalFilename != null ? originalFilename : ""; 
        }
        
        @Override
        public String getContentType() { 
            return contentType; 
        }
        
        @Override
        public boolean isEmpty() { 
            return content == null || content.length == 0; 
        }
        
        @Override
        public long getSize() { 
            return content != null ? content.length : 0; 
        }
        
        @Override
        public byte[] getBytes() throws IOException { 
            if (content == null) {
                throw new IOException("Content is null");
            }
            return content; 
        }
        
        @Override
        public InputStream getInputStream() throws IOException {
            if (content == null) {
                throw new IOException("Content is null");
            }
            return new java.io.ByteArrayInputStream(content);
        }
        
        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            if (content == null) {
                throw new IOException("Content is null");
            }
            if (dest == null) {
                throw new IllegalArgumentException("Destination file cannot be null");
            }
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
                fos.write(content);
            }
        }
    }
}
