package com.twiiiins.exception;

import com.twiiiins.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.warn("리소스를 찾을 수 없음: {} - 요청: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound(ex.getMessage()));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(FileUploadException ex, WebRequest request) {
        log.warn("파일 업로드 오류: {} - 요청: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("유효성 검증 오류: {} - 요청: {}", errorMessage, request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest("입력 데이터가 유효하지 않습니다: " + errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("잘못된 인수 오류: {} - 요청: {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("런타임 오류 발생: {} - 요청: {}", ex.getMessage(), request.getDescription(false), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        String requestPath = request.getDescription(false).replace("uri=", "");
        // 보안 스캐너나 봇이 보내는 무의미한 요청 (PHPUnit, wp-admin 등)은 디버그 레벨로만 로깅
        if (requestPath.contains("phpunit") || requestPath.contains("vendor") || 
            requestPath.contains("wp-admin") || requestPath.contains("wp-login") ||
            requestPath.contains(".env") || requestPath.contains("phpmyadmin")) {
            log.debug("무시된 경로 요청: {} - 요청: {}", ex.getMessage(), requestPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("리소스를 찾을 수 없습니다."));
        }
        log.warn("리소스를 찾을 수 없음: {} - 요청: {}", ex.getMessage(), requestPath);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound("리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, WebRequest request) {
        log.error("예상치 못한 오류 발생: {} - 요청: {}", ex.getMessage(), request.getDescription(false), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("예상치 못한 오류가 발생했습니다."));
    }
}
