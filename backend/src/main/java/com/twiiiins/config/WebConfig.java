package com.twiiiins.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정 클래스
 * 
 * 참고: CORS 설정은 SecurityConfig에서 처리하므로 여기서는 제거했습니다.
 * WebMvcConfigurer와 SecurityConfig의 CORS 설정이 중복되면 충돌이 발생할 수 있습니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 로컬 파일 업로드 경로 (S3 사용 시 필요하지 않을 수 있음)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}

