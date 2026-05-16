package com.twiiiins.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 설정 클래스
 * 
 * 참고: CORS 설정은 SecurityConfig에서 처리하므로 여기서는 제거했습니다.
 * WebMvcConfigurer와 SecurityConfig의 CORS 설정이 중복되면 충돌이 발생할 수 있습니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = resolveUploadLocation();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }

    private String resolveUploadLocation() {
        Path path = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uri = path.toUri().toString();
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        return uri;
    }
}

