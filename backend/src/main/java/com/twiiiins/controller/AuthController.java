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
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody @NonNull LoginRequest request) {
        log.info("로그인 요청: username = {}", request.getUsername());
        LoginResponse response = authService.login(request);
        return ResponseUtil.success(response, "로그인에 성공했습니다.");
    }
}

