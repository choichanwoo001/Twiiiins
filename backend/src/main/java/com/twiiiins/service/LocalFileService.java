package com.twiiiins.service;

import com.twiiiins.exception.FileUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 로컬 파일 시스템 저장소 구현 (기본 파일 저장소).
 */
@Service
@Profile("!s3")
@RequiredArgsConstructor
@Slf4j
public class LocalFileService implements FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:/uploads}")
    private String baseUrl;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        return uploadFileAs(file, folder, fileName);
    }

    @Override
    public String uploadFileAs(MultipartFile file, String folder, String fileName) {
        try {
            Path targetDirectory = resolveTargetDirectory(folder);
            Files.createDirectories(targetDirectory);

            Path targetFile = targetDirectory.resolve(fileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String fileUrl = buildFileUrl(folder, fileName);
            log.info("로컬 저장소에 파일이 업로드되었습니다: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("로컬 파일 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new FileUploadException("로컬 파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public String uploadBytes(byte[] bytes, String folder, String filename) {
        try {
            Path targetDirectory = resolveTargetDirectory(folder);
            Files.createDirectories(targetDirectory);

            Path targetFile = targetDirectory.resolve(filename);
            Files.write(targetFile, bytes);

            String fileUrl = buildFileUrl(folder, filename);
            log.info("로컬 저장소에 바이트 배열이 업로드되었습니다: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("로컬 바이트 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new FileUploadException("로컬 파일 업로드에 실패했습니다.", e);
        }
    }

    private Path resolveTargetDirectory(String folder) {
        Path basePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (StringUtils.hasText(folder)) {
            return basePath.resolve(folder).normalize();
        }
        return basePath;
    }

    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    private String buildFileUrl(String folder, String fileName) {
        String normalizedBase = trimTrailingSlash(baseUrl);
        StringBuilder urlBuilder = new StringBuilder(normalizedBase);

        if (StringUtils.hasText(folder)) {
            for (String segment : folder.split("/")) {
                if (StringUtils.hasText(segment)) {
                    urlBuilder.append("/").append(segment);
                }
            }
        }

        urlBuilder.append("/").append(fileName);
        return urlBuilder.toString();
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String result = value;
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}

