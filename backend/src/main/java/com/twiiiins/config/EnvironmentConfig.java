package com.twiiiins.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EnvironmentConfig {

    static {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();
            
            // 환경변수로 등록
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
            
            System.out.println("환경변수 로드 완료: " + dotenv.entries().size() + " 개의 변수 로드됨");
        } catch (Exception e) {
            System.err.println(".env 파일을 찾을 수 없거나 로드할 수 없습니다: " + e.getMessage());
        }
    }
}
