package com.twiiiins.service;

import com.twiiiins.dto.LoginRequest;
import com.twiiiins.dto.LoginResponse;
import com.twiiiins.entity.User;
import com.twiiiins.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 로그인 처리
     */
    public LoginResponse login(LoginRequest request) {
        log.debug("로그인 시도: username = {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("로그인 실패: 존재하지 않는 사용자 - {}", request.getUsername());
                    return new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
                });
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("로그인 실패: 비밀번호 불일치 - {}", request.getUsername());
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        
        log.info("로그인 성공: {}", request.getUsername());
        return new LoginResponse(user.getUsername(), "로그인에 성공했습니다.");
    }
}

