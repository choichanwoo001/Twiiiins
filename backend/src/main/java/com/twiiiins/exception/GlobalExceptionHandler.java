package com.twiiiins.exception;

import com.twiiiins.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        log.warn("[리소스 없음] 리소스를 찾을 수 없음 - 메시지: {}, 요청 경로: {}, 메서드: {}", 
                ex.getMessage(), requestPath, method);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound(ex.getMessage()));
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiResponse<Object>> handleFileUploadException(FileUploadException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        log.error("[파일 업로드 오류] 파일 업로드 실패 - 메시지: {}, 요청 경로: {}, 메서드: {}", 
                ex.getMessage(), requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex, WebRequest request, HttpServletRequest httpRequest) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        log.warn("[유효성 검증 실패] 입력 데이터 검증 실패 - 필드 오류: {}, 요청 경로: {}, 메서드: {}", 
                errorMessage, requestPath, method);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest("입력 데이터가 유효하지 않습니다: " + errorMessage));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        log.warn("[잘못된 인수] 잘못된 인수 오류 - 메시지: {}, 요청 경로: {}, 메서드: {}", 
                ex.getMessage(), requestPath, method);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest(ex.getMessage()));
    }

    // 데이터베이스 관련 예외 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        Throwable rootCause = ex.getRootCause();
        String rootCauseMessage = rootCause != null ? rootCause.getMessage() : "알 수 없음";
        log.error("[데이터베이스 무결성 오류] 데이터 무결성 제약 조건 위반 - 메시지: {}, 원인: {}, 요청 경로: {}, 메서드: {}", 
                ex.getMessage(), rootCauseMessage, requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.badRequest("데이터 무결성 오류가 발생했습니다. 중복된 데이터가 있는지 확인해주세요."));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataAccessException(DataAccessException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        Throwable rootCause = ex.getRootCause();
        String rootCauseMessage = rootCause != null ? rootCause.getMessage() : "알 수 없음";
        log.error("[데이터베이스 접근 오류] 데이터베이스 접근 실패 - 메시지: {}, 원인: {}, 요청 경로: {}, 메서드: {}", 
                ex.getMessage(), rootCauseMessage, requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("데이터베이스 접근 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Object>> handleSQLException(SQLException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        log.error("[SQL 오류] SQL 실행 실패 - SQL 상태: {}, 오류 코드: {}, 메시지: {}, 요청 경로: {}, 메서드: {}", 
                ex.getSQLState(), ex.getErrorCode(), ex.getMessage(), requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("데이터베이스 쿼리 실행 중 오류가 발생했습니다."));
    }

    // IO 관련 예외 처리
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Object>> handleIOException(IOException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        log.error("[IO 오류] 파일 입출력 실패 - 메시지: {}, 요청 경로: {}, 메서드: {}", 
                ex.getMessage(), requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("파일 처리 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<ApiResponse<Object>> handleSocketTimeoutException(SocketTimeoutException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        log.error("[타임아웃 오류] 소켓 타임아웃 발생 - 메시지: {}, 요청 경로: {}, 메서드: {}", 
                ex.getMessage(), requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                .body(ApiResponse.internalError("요청 시간이 초과되었습니다."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        String exceptionType = ex.getClass().getSimpleName();
        log.error("[런타임 오류] {} - 메시지: {}, 요청 경로: {}, 메서드: {}", 
                exceptionType, ex.getMessage(), requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("서버 내부 오류가 발생했습니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        // 보안 스캐너나 봇이 보내는 무의미한 요청 (PHPUnit, wp-admin 등)은 디버그 레벨로만 로깅
        if (requestPath.contains("phpunit") || requestPath.contains("vendor") || 
            requestPath.contains("wp-admin") || requestPath.contains("wp-login") ||
            requestPath.contains(".env") || requestPath.contains("phpmyadmin")) {
            log.debug("[무시된 경로] 보안 스캐너 요청 감지 - 경로: {}", requestPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.notFound("리소스를 찾을 수 없습니다."));
        }
        log.warn("[리소스 없음] 경로를 찾을 수 없음 - 경로: {}, 메서드: {}", requestPath, method);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.notFound("리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex, WebRequest request, HttpServletRequest httpRequest) {
        String requestPath = extractRequestPath(request);
        String method = httpRequest != null ? httpRequest.getMethod() : "UNKNOWN";
        String exceptionType = ex.getClass().getName();
        log.error("[예상치 못한 오류] 처리되지 않은 예외 - 타입: {}, 메시지: {}, 요청 경로: {}, 메서드: {}", 
                exceptionType, ex.getMessage(), requestPath, method, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError("예상치 못한 오류가 발생했습니다."));
    }

    // 헬퍼 메서드들
    private String extractRequestPath(WebRequest request) {
        String description = request.getDescription(false);
        if (description.contains("uri=")) {
            return description.replace("uri=", "");
        }
        return description;
    }
}
