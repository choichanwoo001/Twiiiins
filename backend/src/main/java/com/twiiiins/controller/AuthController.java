package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.LoginRequest;
import com.twiiiins.dto.LoginResponse;
import com.twiiiins.service.AuthService;
import com.twiiiins.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final com.twiiiins.security.AdminSessionRepository sessions;
    private final com.twiiiins.newsletter.RequestThrottle throttle;

    @GetMapping("/me")
    public ApiResponse<?> me(java.security.Principal principal) {
        return ApiResponse.success(java.util.Map.of("username", principal.getName()));
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(@RequestHeader("Authorization") String authorization) {
        sessions.deleteById(com.twiiiins.security.Tokens.hash(authorization.substring(7)));
        return ApiResponse.success("Logged out");
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody @NonNull LoginRequest request, jakarta.servlet.http.HttpServletRequest http) {
        if (!throttle.allow("login:" + http.getRemoteAddr(), 10, 900))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Please try again later.");
        log.info("로그인 요청: username = {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseUtil.success(response, "로그인에 성공했습니다.");
    }
}

