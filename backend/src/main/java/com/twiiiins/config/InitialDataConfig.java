package com.twiiiins.config;

import com.twiiiins.entity.User;
import com.twiiiins.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InitialDataConfig {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    public CommandLineRunner initializeDefaultUser() {
        return args -> {
            String defaultUsername = System.getenv("DEFAULT_ADMIN_USERNAME");
            String defaultPassword = System.getenv("DEFAULT_ADMIN_PASSWORD");
            if (defaultUsername == null || defaultUsername.isBlank() || defaultPassword == null || defaultPassword.isBlank()) {
                log.info("Administrator bootstrap skipped: explicit credentials are required for new accounts.");
                return;
            }
            // 기존 사용자가 있는지 확인
            if (userRepository.findByUsername(defaultUsername).isEmpty()) {
                // 비밀번호를 BCrypt로 해시화
                String hashedPassword = passwordEncoder.encode(defaultPassword);
                
                User defaultUser = new User();
                defaultUser.setUsername(defaultUsername);
                defaultUser.setPassword(hashedPassword);
                
                userRepository.save(defaultUser);
                // 보안: 비밀번호는 로그에 출력하지 않음
                log.info("✅ 초기 사용자 생성 완료: username = {}", defaultUsername);
            } else {
                log.debug("ℹ️  기본 사용자({})가 이미 DB에 존재합니다. 중복 생성하지 않습니다.", defaultUsername);
            }
        };
    }
}

