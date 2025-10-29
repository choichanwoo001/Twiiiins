package com.twiiiins.config;

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
@Configuration
public class S3Config {

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    @Value("${s3.bucket-name:}")
    private String bucketName;

    /**
     * S3 클라이언트 빈 생성
     * @return S3Client 인스턴스
     */
    @Bean
    public S3Client s3Client() {
        // AWS 자격 증명이 설정되지 않은 경우 기본 클라이언트 반환
        if (accessKeyId.isEmpty() || secretAccessKey.isEmpty()) {
            return S3Client.builder()
                    .region(Region.of(region))
                    .build();
        }

        // 설정된 자격 증명으로 S3 클라이언트 생성
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
