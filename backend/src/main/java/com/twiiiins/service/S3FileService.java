package com.twiiiins.service;

import com.twiiiins.config.S3Config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

/**
 * AWS S3 파일 업로드 서비스
 * 로컬 파일 시스템 대신 S3에 파일 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Client s3Client;
    private final S3Config s3Config;

    /**
     * 파일을 S3에 업로드
     * @param file 업로드할 파일
     * @param folder S3 내 폴더 경로 (예: "uploads", "images")
     * @return 업로드된 파일의 S3 URL
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String bucketName = s3Config.getBucketName();
            if (bucketName == null || bucketName.isEmpty()) {
                log.warn("S3 버킷 이름이 설정되지 않았습니다. 로컬 저장을 사용합니다.");
                return null;
            }

            // 고유한 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + extension;
            String key = folder + "/" + fileName;

            // S3에 파일 업로드 (공개 읽기 권한으로 설정)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .acl(ObjectCannedACL.PUBLIC_READ) // 공개 읽기 권한 설정
                    .build();

            // MultipartFile의 inputStream은 한 번만 읽을 수 있으므로 바이트 배열로 먼저 읽기
            byte[] fileBytes = file.getBytes();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileBytes));

            // 업로드된 파일의 URL 생성
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", 
                    bucketName, s3Config.getRegion(), key);
            
            log.info("파일이 S3에 성공적으로 업로드되었습니다: {}", fileUrl);
            return fileUrl;

        } catch (S3Exception e) {
            log.error("S3 업로드 중 오류 발생 (버킷: {}, 에러 코드: {}): {}", 
                    s3Config.getBucketName(), e.awsErrorDetails().errorCode(), e.getMessage());
            log.error("S3 예외 상세:", e);
            return null; // null 반환하여 로컬 저장소로 fallback
        } catch (IOException e) {
            log.error("파일 읽기 중 오류 발생: {}", e.getMessage(), e);
            return null; // null 반환하여 로컬 저장소로 fallback
        } catch (Exception e) {
            log.error("S3 업로드 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return null; // null 반환하여 로컬 저장소로 fallback
        }
    }

    /**
     * S3에서 파일 삭제
     * @param fileUrl 삭제할 파일의 S3 URL
     */
    public void deleteFile(String fileUrl) {
        try {
            String bucketName = s3Config.getBucketName();
            if (bucketName.isEmpty()) {
                log.warn("S3 버킷 이름이 설정되지 않았습니다.");
                return;
            }

            // URL에서 S3 키 추출
            String key = extractKeyFromUrl(fileUrl);
            if (key == null) {
                log.warn("유효하지 않은 S3 URL입니다: {}", fileUrl);
                return;
            }

            // S3에서 파일 삭제
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("파일이 S3에서 성공적으로 삭제되었습니다: {}", fileUrl);

        } catch (Exception e) {
            log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 파일 확장자 추출
     * @param filename 파일명
     * @return 확장자 (점 포함)
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    /**
     * S3 URL에서 키 추출
     * @param fileUrl S3 파일 URL
     * @return S3 키
     */
    private String extractKeyFromUrl(String fileUrl) {
        try {
            // https://bucket-name.s3.region.amazonaws.com/folder/filename 형식에서 키 추출
            String[] parts = fileUrl.split("/");
            if (parts.length >= 4) {
                StringBuilder key = new StringBuilder();
                for (int i = 3; i < parts.length; i++) {
                    if (key.length() > 0) {
                        key.append("/");
                    }
                    key.append(parts[i]);
                }
                return key.toString();
            }
        } catch (Exception e) {
            log.error("URL에서 키 추출 중 오류 발생: {}", e.getMessage());
        }
        return null;
    }

    /**
     * S3 버킷 존재 여부 확인
     * @return 버킷 존재 여부
     */
    public boolean isBucketAvailable() {
        try {
            String bucketName = s3Config.getBucketName();
            if (bucketName.isEmpty()) {
                return false;
            }

            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            return true;

        } catch (NoSuchBucketException e) {
            log.warn("S3 버킷이 존재하지 않습니다: {}", s3Config.getBucketName());
            return false;
        } catch (Exception e) {
            log.error("S3 버킷 확인 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}
