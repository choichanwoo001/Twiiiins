package com.twiiiins.util;

import com.twiiiins.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * API 응답을 위한 유틸리티 클래스
 * 컨트롤러에서 반복되는 응답 패턴을 통합
 */
public class ResponseUtil {
    
    /**
     * 성공 응답 (200 OK)
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * 성공 응답 (200 OK) with 메시지
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
    
    /**
     * 생성 성공 응답 (201 Created)
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }
    
    /**
     * 삭제 성공 응답 (200 OK)
     */
    public static ResponseEntity<ApiResponse<Void>> deleted(String message) {
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }
    
    /**
     * 목록 조회 성공 응답
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> listSuccess(List<T> data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }
    
    /**
     * 목록 조회 성공 응답 (기본 메시지)
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> listSuccess(List<T> data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
