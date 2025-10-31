package com.twiiiins.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS S3 설정 클래스
 * 파일 업로드를 위한 S3 클라이언트 구성
 */
@Slf4j
@Configuration
public class S3Config {

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.region:eu-central-1}")
    private String region;

    @Value("${s3.bucket-name:}")
    private String bucketName;
    
    @Bean
    public String s3ConfigInitializer() {
        // S3 설정 로드 확인을 위한 로그
        log.info("=== S3 설정 로드 상태 ===");
        log.info("AWS Region: {}", region);
        log.info("Bucket Name: {}", (bucketName != null && !bucketName.isEmpty() ? bucketName : "❌ 설정되지 않음"));
        log.info("Access Key ID: {}", (accessKeyId != null && !accessKeyId.isEmpty() ? "✅ 설정됨" : "❌ 설정되지 않음"));
        log.info("Secret Access Key: {}", (secretAccessKey != null && !secretAccessKey.isEmpty() ? "✅ 설정됨" : "❌ 설정되지 않음"));
        log.info("========================");
        return "S3Config initialized";
    }

    /**
     * S3 클라이언트 빈 생성
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        // AWS 자격 증명이 설정되지 않은 경우 기본 클라이언트 반환
        if (accessKeyId == null || accessKeyId.isEmpty() || 
            secretAccessKey == null || secretAccessKey.isEmpty()) {
            log.warn("AWS 자격 증명이 설정되지 않았습니다. 기본 S3 클라이언트를 생성합니다.");
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        }

        // 설정된 자격 증명으로 S3 클라이언트 생성
        log.info("AWS 자격 증명으로 S3 클라이언트를 생성합니다. Region: {}", region);
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    /**
     * S3 버킷 이름 반환
     * @return 버킷 이름
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * AWS 리전 반환
     * @return 리전
     */
    public String getRegion() {
        return region;
    }
}
