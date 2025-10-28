package com.twiiiins.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class EnvironmentConfig {

    @PostConstruct
    public void loadEnvironmentVariables() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();
            
            // 환경변수로 등록
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
            
            log.info("환경변수 로드 완료: {} 개의 변수 로드됨", dotenv.entries().size());
        } catch (Exception e) {
            log.warn(".env 파일을 찾을 수 없거나 로드할 수 없습니다: {}", e.getMessage());
        }
    }
}
