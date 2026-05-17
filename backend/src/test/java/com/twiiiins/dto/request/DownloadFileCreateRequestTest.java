package com.twiiiins.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadFileCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("/uploads/ 경로 fileUrl은 검증에 성공해야 한다")
    void shouldAcceptUploadsPath() {
        DownloadFileCreateRequest request = new DownloadFileCreateRequest();
        request.setName("Portfolio");
        request.setFileUrl("/uploads/file/abc.pdf");

        Set<ConstraintViolation<DownloadFileCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("https S3 URL fileUrl은 검증에 성공해야 한다")
    void shouldAcceptHttpsS3Url() {
        DownloadFileCreateRequest request = new DownloadFileCreateRequest();
        request.setName("Portfolio");
        request.setFileUrl("https://bucket.s3.amazonaws.com/key.pdf");

        Set<ConstraintViolation<DownloadFileCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("허용되지 않는 fileUrl은 검증에 실패해야 한다")
    void shouldRejectInvalidFileUrl() {
        DownloadFileCreateRequest request = new DownloadFileCreateRequest();
        request.setName("Portfolio");
        request.setFileUrl("javascript:alert(1)");

        Set<ConstraintViolation<DownloadFileCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("fileUrl"));
    }
}
