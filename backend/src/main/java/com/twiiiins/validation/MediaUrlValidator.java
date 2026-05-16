package com.twiiiins.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * ValidMediaUrl 어노테이션 검증 구현체.
 * 다음 패턴을 허용합니다:
 *   - null 또는 빈 문자열 (필수 여부는 @NotBlank로 별도 처리)
 *   - http:// 또는 https:// 로 시작하는 절대 URL
 *   - /uploads/ 로 시작하는 로컬 파일 저장 경로
 */
public class MediaUrlValidator implements ConstraintValidator<ValidMediaUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return value.startsWith("http://")
                || value.startsWith("https://")
                || value.startsWith("/uploads/");
    }
}
