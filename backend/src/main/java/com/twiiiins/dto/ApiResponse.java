package com.twiiiins.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    private Integer code;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data, null, LocalDateTime.now(), 200);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now(), 200);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, LocalDateTime.now(), 200);
    }

    public static <T> ApiResponse<T> error(String error, int code) {
        return new ApiResponse<>(false, null, null, error, LocalDateTime.now(), code);
    }

    public static <T> ApiResponse<T> error(String error, String message, int code) {
        return new ApiResponse<>(false, message, null, error, LocalDateTime.now(), code);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, "리소스가 성공적으로 생성되었습니다.", data, null, LocalDateTime.now(), 201);
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now(), 201);
    }

    public static <T> ApiResponse<T> noContent() {
        return new ApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", null, null, LocalDateTime.now(), 204);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, message, null, "RESOURCE_NOT_FOUND", LocalDateTime.now(), 404);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(false, message, null, "BAD_REQUEST", LocalDateTime.now(), 400);
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(false, message, null, "INTERNAL_SERVER_ERROR", LocalDateTime.now(), 500);
    }
}
