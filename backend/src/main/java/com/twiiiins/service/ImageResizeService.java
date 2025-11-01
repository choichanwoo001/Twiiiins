package com.twiiiins.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 이미지 리사이징 서비스
 * 썸네일 생성 및 이미지 크기 조정
 */
@Slf4j
@Service
public class ImageResizeService {

    /**
     * 이미지 썸네일 생성
     * @param file 원본 이미지 파일
     * @param maxWidth 최대 너비
     * @param maxHeight 최대 높이
     * @param quality 이미지 품질 (0.0 ~ 1.0)
     * @return 리사이즈된 이미지 바이트 배열
     */
    public byte[] generateThumbnail(MultipartFile file, int maxWidth, int maxHeight, float quality) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(inputStream);
            
            if (originalImage == null) {
                throw new IOException("이미지를 읽을 수 없습니다: " + file.getOriginalFilename());
            }

            // 비율 유지하며 리사이즈 크기 계산
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            double widthRatio = (double) maxWidth / originalWidth;
            double heightRatio = (double) maxHeight / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);
            
            int newWidth = (int) (originalWidth * ratio);
            int newHeight = (int) (originalHeight * ratio);
            
            // 이미지가 작으면 리사이즈 불필요
            if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
                log.debug("이미지가 이미 작은 크기입니다. 원본 사용: {}x{}", originalWidth, originalHeight);
                return file.getBytes();
            }

            // 고품질 리사이징 (Bilinear interpolation)
            Image scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            // 바이트 배열로 변환
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String formatName = getFormatName(file.getOriginalFilename());
            
            // JPEG 품질 설정이 필요한 경우
            if ("jpg".equalsIgnoreCase(formatName) || "jpeg".equalsIgnoreCase(formatName)) {
                try {
                    javax.imageio.ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                    javax.imageio.stream.ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                    writer.setOutput(ios);
                    
                    javax.imageio.ImageWriteParam writeParam = writer.getDefaultWriteParam();
                    if (writeParam.canWriteCompressed()) {
                        writeParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                        writeParam.setCompressionQuality(quality);
                    }
                    
                    writer.write(null, new javax.imageio.IIOImage(resizedImage, null, null), writeParam);
                    writer.dispose();
                    ios.close();
                } catch (Exception e) {
                    log.warn("고품질 JPEG 저장 실패, 기본 방식 사용: {}", e.getMessage());
                    ImageIO.write(resizedImage, formatName, baos);
                }
            } else {
                ImageIO.write(resizedImage, formatName, baos);
            }
            
            byte[] resizedBytes = baos.toByteArray();
            log.info("썸네일 생성 완료: {}x{} -> {}x{} (원본: {} bytes, 썸네일: {} bytes)", 
                    originalWidth, originalHeight, newWidth, newHeight, file.getSize(), resizedBytes.length);
            
            return resizedBytes;
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

