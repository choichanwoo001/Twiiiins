package com.twiiiins.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 이미지 리사이징 서비스
 * 썸네일 생성 및 이미지 크기 조정
 * 메모리 효율적인 Thumbnailator 라이브러리 사용
 */
@Slf4j
@Service
public class ImageResizeService {

    /**
     * 이미지 썸네일 생성
     * 메모리 효율적인 Thumbnailator를 사용하여 대용량 이미지 처리
     * @param file 원본 이미지 파일
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     * @param quality 이미지 품질 (0.0 ~ 1.0)
     * @return 리사이즈된 이미지 바이트 배열
     */
    public byte[] generateThumbnail(MultipartFile file, int maxWidth, int maxHeight, float quality) throws IOException {
        // 이미지 크기 확인 (Thumbnailator 사용)
        int originalWidth = 0;
        int originalHeight = 0;
        
        try (InputStream inputStream = file.getInputStream()) {
            // Thumbnailator를 사용하여 이미지 크기 확인
            BufferedImage bufferedImage = Thumbnails.of(inputStream)
                    .scale(1.0)
                    .asBufferedImage();
            
            if (bufferedImage != null) {
                originalWidth = bufferedImage.getWidth();
                originalHeight = bufferedImage.getHeight();
                
                // 이미지가 이미 작으면 리사이즈 불필요
                if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                    log.debug("이미지가 이미 작은 크기입니다. 원본 사용: {}x{}", originalWidth, originalHeight);
                    try {
                        return file.getBytes();
                    } catch (IOException e) {
                        log.warn("원본 파일 읽기 실패, 리사이징으로 대체: {}", e.getMessage());
                        // 원본 읽기 실패 시 리사이징으로 진행
                    }
                }
            }
        } catch (Exception e) {
            log.warn("이미지 크기 확인 실패, Thumbnailator로 직접 처리: {}", e.getMessage());
            // 크기 확인 실패 시에도 Thumbnailator로 처리 시도
        }
        
        // Thumbnailator를 사용하여 메모리 효율적으로 리사이징
        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            String formatName = getFormatName(file.getOriginalFilename());
            
            // Thumbnailator는 메모리 효율적으로 이미지를 처리합니다
            // 대용량 이미지의 경우 스트리밍 방식으로 처리하여 메모리 사용량을 최소화합니다
            Thumbnails.of(inputStream)
                    .size(maxWidth, maxHeight)
                    .outputFormat(formatName)
                    .outputQuality(quality)
                    .toOutputStream(baos);
            
            byte[] resizedBytes = baos.toByteArray();
            log.info("썸네일 생성 완료: {}x{} -> 최대 {}x{} (원본: {} bytes, 썸네일: {} bytes)", 
                    originalWidth, originalHeight, maxWidth, maxHeight, file.getSize(), resizedBytes.length);
            
            return resizedBytes;
        } catch (OutOfMemoryError e) {
            log.error("메모리 부족으로 썸네일 생성 실패: {}", file.getOriginalFilename(), e);
            throw new IOException("이미지가 너무 커서 처리할 수 없습니다. 더 작은 이미지를 사용해주세요.", e);
        } catch (Exception e) {
            log.error("썸네일 생성 실패: {}", e.getMessage(), e);
            throw new IOException("썸네일 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일명에서 이미지 포맷 추출
     */
    private String getFormatName(String filename) {
        if (filename == null) {
            return "jpg";
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "png";
        } else if (lower.endsWith(".gif")) {
            return "gif";
        } else if (lower.endsWith(".webp")) {
            return "webp";
        }
        return "jpg"; // 기본값
    }
}

