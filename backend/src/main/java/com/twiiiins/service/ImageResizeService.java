package com.twiiiins.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

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
        // 이미지 크기만 확인 (메모리에 전체 이미지 로드하지 않음)
        int originalWidth = 0;
        int originalHeight = 0;
        
        try (InputStream inputStream = file.getInputStream()) {
            // ImageReader를 사용하여 메타데이터만 읽어 이미지 크기 확인
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            
            if (!readers.hasNext()) {
                throw new IOException("지원하지 않는 이미지 형식입니다: " + file.getOriginalFilename());
            }
            
            ImageReader reader = readers.next();
            reader.setInput(imageInputStream);
            originalWidth = reader.getWidth(0);
            originalHeight = reader.getHeight(0);
            reader.dispose();
            imageInputStream.close();
            
            // 이미지가 이미 작으면 리사이즈 불필요
            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                log.debug("이미지가 이미 작은 크기입니다. 원본 사용: {}x{}", originalWidth, originalHeight);
                return file.getBytes();
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

