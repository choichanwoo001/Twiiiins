package com.twiiiins.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장소 인터페이스
 * 파일 저장소 구현체를 위한 공통 계약
 */
public interface FileStorageService {

    /**
     * 파일을 저장하고 접근 가능한 URL을 반환합니다.
     *
     * @param file   저장할 파일
     * @param folder 저장할 폴더 경로 (예: image, file/thumbnails)
     * @return 저장된 파일의 접근 URL
     */
    String uploadFile(MultipartFile file, String folder);

    /**
     * 바이트 배열을 지정된 파일명으로 저장하고 접근 가능한 URL을 반환합니다.
     * 리사이즈된 이미지를 원본과 같은 UUID 파일명으로 저장할 때 사용합니다.
     *
     * @param bytes    저장할 바이트 배열
     * @param folder   저장할 폴더 경로
     * @param filename 저장할 파일명 (확장자 포함)
     * @return 저장된 파일의 접근 URL
     */
    String uploadBytes(byte[] bytes, String folder, String filename);
}

