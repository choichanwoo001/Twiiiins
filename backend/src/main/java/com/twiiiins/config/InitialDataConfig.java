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
            // 기존에 dowon 사용자가 있는지 확인
            if (userRepository.findByUsername("dowon").isEmpty()) {
                // 비밀번호 "1234"를 BCrypt로 해시화
                String hashedPassword = passwordEncoder.encode("1234");
                
                User defaultUser = new User();
                defaultUser.setUsername("dowon");
                defaultUser.setPassword(hashedPassword);
                
                userRepository.save(defaultUser);
                log.info("✅ 초기 사용자 생성 완료: username = dowon, password = 1234 (해시화됨)");
            } else {
                log.debug("ℹ️  기본 사용자(dowon)가 이미 DB에 존재합니다. 중복 생성하지 않습니다.");
            }
        };
    }
}

