package com.twiiiins.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 미디어 URL 검증 어노테이션.
 * http(s):// 절대 URL과 /uploads/ 상대 경로를 모두 허용합니다.
 */
@Documented
@Target({FIELD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = MediaUrlValidator.class)
public @interface ValidMediaUrl {

    String message() default "유효한 미디어 URL이 아닙니다. (http/https URL 또는 /uploads/ 경로만 허용)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
