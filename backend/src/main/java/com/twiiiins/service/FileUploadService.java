package com.twiiiins.service;

import com.twiiiins.dto.FileUploadResponseDto;
import com.twiiiins.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileUploadService {
    
    private final FileStorageService fileStorageService;
    private final ImageResizeService imageResizeService;
    private static final int[] IMAGE_VARIANT_WIDTHS = {640, 1280, 1920};
    private static final float IMAGE_VARIANT_QUALITY = 0.86f;
    private static final int THUMBNAIL_SIZE = 400;
    
    @Value("${UPLOAD_MAX_SIZE:100MB}")
    private String uploadMaxSize;
    
    private long getMaxFileSize() {
        try {
            return DataSize.parse(Objects.requireNonNull(uploadMaxSize, "UPLOAD_MAX_SIZE must not be null")).toBytes();
        } catch (Exception e) {
            log.warn("UPLOAD_MAX_SIZE 파싱 실패, 기본값 100MB 사용: {}", e.getMessage());
            return 100 * 1024 * 1024; // 기본값 100MB
        }
    }
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "application/pdf"
    );
    
    public FileUploadResponseDto uploadImage(@NonNull MultipartFile file) {
        log.info("이미지 파일 업로드 시작: 원본 파일명 = {}, 크기 = {} bytes", 
                file.getOriginalFilename(), file.getSize());
        validateFile(file, ALLOWED_IMAGE_TYPES, "이미지 파일만 업로드 가능합니다.");
        return uploadFileWithVariants(file, "image");
    }
    
    /**
     * 이미지와 썸네일을 함께 업로드
     * @param file 원본 이미지 파일
     * @return 원본 URL과 썸네일 URL을 포함한 응답 (thumbnailUrl 필드 추가 필요 시 별도 DTO 사용)
     */
    public FileUploadResponseDto uploadImageWithThumbnail(@NonNull MultipartFile file) {
        log.info("이미지 및 썸네일 업로드 시작: 원본 파일명 = {}, 크기 = {} bytes", 
                file.getOriginalFilename(), file.getSize());
        validateFile(file, ALLOWED_IMAGE_TYPES, "이미지 파일만 업로드 가능합니다.");
        return uploadFileWithThumbnail(file, "image");
    }
    
    public FileUploadResponseDto uploadFile(@NonNull MultipartFile file) {
        log.info("파일 업로드 시작: 원본 파일명 = {}, 크기 = {} bytes, MIME 타입 = {}", 
                file.getOriginalFilename(), file.getSize(), file.getContentType());
        validateFile(file, ALLOWED_FILE_TYPES, "이미지 파일 또는 PDF 파일만 업로드 가능합니다.");
        return uploadFile(file, "file");
    }

    public Map<String, Object> uploadImages(MultipartFile[] files) {
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
                FileUploadResponseDto response = uploadImage(file);
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
        return data;
    }
    
    private void validateFile(@NonNull MultipartFile file, List<String> allowedTypes, String errorMessage) {
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
        
        // 이미지 파일인 경우 픽셀 크기 검증 (메모리 문제 방지)
        if (contentType != null && contentType.startsWith("image/")) {
            validateImageDimensions(file);
        }
        
        log.debug("파일 검증 완료: 파일명 = {}, 타입 = {}", file.getOriginalFilename(), contentType);
    }
    
    /**
     * 이미지 픽셀 크기 검증 (메모리 부족 방지)
     * 너무 큰 이미지는 처리 시 OutOfMemoryError 발생 가능
     * Thumbnailator를 사용하여 메모리 효율적으로 이미지 크기 확인
     */
    private void validateImageDimensions(@NonNull MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            // Thumbnailator를 사용하여 이미지 크기 확인 (메모리 효율적)
            BufferedImage bufferedImage = Thumbnails.of(inputStream)
                    .scale(1.0) // 원본 크기 유지
                    .asBufferedImage();
            
            if (bufferedImage != null) {
                int width = bufferedImage.getWidth();
                int height = bufferedImage.getHeight();
                
                // 최대 픽셀 크기 제한: 20000x20000
                // Thumbnailator를 사용하면 메모리 효율적으로 처리 가능
                // 일반적인 60~70MB 고품질 이미지도 처리 가능 (예: 8000x6000 정도)
                int maxDimension = 20000;
                
                // 총 픽셀 수로도 검증 (메모리 사용량 추정)
                long totalPixels = (long) width * height;
                long maxPixels = (long) maxDimension * maxDimension;
                
                if (width > maxDimension || height > maxDimension || totalPixels > maxPixels) {
                    log.warn("이미지 픽셀 크기 초과: {}x{} (총 픽셀: {}, 최대: {}x{} = {})", 
                            width, height, totalPixels, maxDimension, maxDimension, maxPixels);
                    throw new FileUploadException(
                        String.format("이미지 크기가 너무 큽니다. 최대 %dx%d 픽셀까지 지원됩니다. (현재: %dx%d)", 
                        maxDimension, maxDimension, width, height));
                }
                
                log.debug("이미지 픽셀 크기 검증 완료: {}x{}", width, height);
            }
        } catch (FileUploadException e) {
            throw e;
        } catch (NoClassDefFoundError | Exception e) {
            log.warn("이미지 크기 검증 실패 (계속 진행): {}", e.getMessage());
            // 검증 실패해도 업로드는 진행 (이미지가 손상되었거나 형식이 예상과 다를 수 있음)
            // 또는 java.desktop 모듈이 사용 불가능한 환경일 수 있음
        }
    }
    
    private FileUploadResponseDto uploadFile(@NonNull MultipartFile file, @NonNull String uploadType) {
        String originalFilename = file.getOriginalFilename();

        try {
            String extension = getFileExtension(originalFilename);
            String uuid = UUID.randomUUID().toString();
            String webFilename = uuid + extension;

            // 1. 원본 그대로 originals/ 에 보관 (DB에 저장하지 않음)
            try {
                fileStorageService.uploadBytes(file.getBytes(), uploadType + "/originals", webFilename);
                log.info("원본 파일 보관 완료: {}/{}/{}", uploadType, "originals", webFilename);
            } catch (Exception e) {
                log.warn("원본 보관 실패 (업로드는 계속 진행): {}", e.getMessage());
            }

            // 2. 웹용 리사이즈본 저장 → imageUrl (DB 저장)
            int maxLongEdge = resolveMaxLongEdge(uploadType);
            byte[] webBytes;
            try {
                webBytes = imageResizeService.resizeToMaxLongEdge(file, maxLongEdge, 0.85f);
            } catch (Exception e) {
                log.warn("리사이즈 실패, 원본 바이트 사용: {}", e.getMessage());
                webBytes = file.getBytes();
            }
            String fileUrl = fileStorageService.uploadBytes(webBytes, uploadType, webFilename);

            log.info("파일 업로드 완료: {} -> {}", originalFilename, fileUrl);
            return new FileUploadResponseDto(
                fileUrl,
                null,
                webFilename,
                originalFilename,
                file.getSize(),
                file.getContentType()
            );
        } catch (FileUploadException e) {
            log.error("[파일 업로드] 업로드 예외 - 파일명: {}, 오류: {}", originalFilename, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[파일 업로드] 예상치 못한 오류 - 파일명: {}, 오류 타입: {}, 오류: {}",
                    originalFilename, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new FileUploadException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 업로드 타입별 최대 긴 변 픽셀 크기 반환
     */
    private int resolveMaxLongEdge(@NonNull String uploadType) {
        if (uploadType.startsWith("image")) {
            // 기본값은 1920px; equipment, music 등 커버 이미지는 800px
            return 1920;
        }
        return 1920;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) return "";
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot) : "";
    }
    
    /**
     * 이미지와 썸네일을 함께 업로드하는 메서드 (Photo 전용)
     * UUID를 공유하여 originals/ 원본 + 웹용 리사이즈본 + 썸네일 세 가지를 저장합니다.
     */
    private FileUploadResponseDto uploadFileWithVariants(@NonNull MultipartFile file, @NonNull String uploadType) {
        FileUploadResponseDto response = uploadFile(file, uploadType);
        String firstVariantUrl = generateImageVariants(file, response.getUrl(), uploadType);
        if (firstVariantUrl != null) {
            response.setThumbnailUrl(firstVariantUrl);
        }
        return response;
    }

    private String generateImageVariants(@NonNull MultipartFile file, String originalUrl, @NonNull String uploadType) {
        String contentType = file.getContentType();
        if (contentType == null || contentType.equalsIgnoreCase("image/gif")) {
            return null;
        }

        String storedFileName = extractFileName(originalUrl);
        String baseName = stripExtension(storedFileName);
        if (baseName == null || baseName.isBlank()) {
            return null;
        }

        String firstVariantUrl = null;
        for (int width : IMAGE_VARIANT_WIDTHS) {
            try {
                byte[] variantBytes = imageResizeService.generateJpegVariant(file, width, IMAGE_VARIANT_QUALITY);
                String variantFileName = baseName + "-" + width + ".jpg";
                String variantUrl = fileStorageService.uploadBytes(variantBytes, uploadType + "/variants", variantFileName);
                if (firstVariantUrl == null) {
                    firstVariantUrl = variantUrl;
                }
            } catch (Exception e) {
                log.warn("Image variant generation failed (width={}): {}", width, e.getMessage());
            }
        }
        return firstVariantUrl;
    }

    private String stripExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

    private String extractFileName(String url) {
        if (url == null || url.isBlank()) return "";
        String path = url.split("\\?")[0];
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private FileUploadResponseDto uploadFileWithThumbnail(@NonNull MultipartFile file, @NonNull String uploadType) {
        String originalFilename = file.getOriginalFilename();

        try {
            String extension = getFileExtension(originalFilename);
            String uuid = UUID.randomUUID().toString();
            String webFilename = uuid + extension;

            // 1. 원본 그대로 originals/ 에 보관 (DB에 저장하지 않음)
            try {
                fileStorageService.uploadBytes(file.getBytes(), uploadType + "/originals", webFilename);
                log.info("원본 파일 보관 완료: {}/{}", uploadType + "/originals", webFilename);
            } catch (Exception e) {
                log.warn("원본 보관 실패 (업로드는 계속 진행): {}", e.getMessage());
            }

            // 2. 웹용 리사이즈본 (1920px) → imageUrl (DB 저장)
            byte[] webBytes;
            try {
                webBytes = imageResizeService.resizeToMaxLongEdge(file, 1920, 0.85f);
            } catch (Exception e) {
                log.warn("웹용 리사이즈 실패, 원본 바이트 사용: {}", e.getMessage());
                webBytes = file.getBytes();
            }
            String fileUrl = fileStorageService.uploadBytes(webBytes, uploadType, webFilename);
            log.info("웹용 이미지 업로드 완료: {} -> {}", originalFilename, fileUrl);

            // 3. 썸네일 생성 및 업로드 → thumbnailUrl (DB 저장)
            String thumbnailUrl = null;
            try {
                byte[] thumbnailBytes = imageResizeService.generateThumbnail(file, THUMBNAIL_SIZE, THUMBNAIL_SIZE, 0.85f);
                thumbnailUrl = fileStorageService.uploadBytes(thumbnailBytes, uploadType + "/thumbnails", webFilename);
                log.info("썸네일 업로드 완료: {}", thumbnailUrl);
            } catch (Exception thumbnailError) {
                log.warn("썸네일 생성/업로드 실패 (원본은 업로드됨): {}", thumbnailError.getMessage());
            }

            return new FileUploadResponseDto(
                fileUrl,
                thumbnailUrl,
                webFilename,
                originalFilename,
                file.getSize(),
                file.getContentType()
            );

        } catch (FileUploadException e) {
            log.error("[이미지 업로드] 파일 업로드 예외 - 파일명: {}, 오류: {}",
                    originalFilename, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[이미지 업로드] 예상치 못한 오류 - 파일명: {}, 오류 타입: {}, 오류: {}",
                    originalFilename, e.getClass().getSimpleName(), e.getMessage(), e);
            throw new FileUploadException("이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }
}

