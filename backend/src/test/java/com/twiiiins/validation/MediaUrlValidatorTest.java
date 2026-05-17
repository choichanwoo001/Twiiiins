package com.twiiiins.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MediaUrlValidatorTest {

    private MediaUrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new MediaUrlValidator();
        validator.initialize(mock(ValidMediaUrl.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/uploads/file/abc.pdf",
            "/uploads/image/uuid.jpg",
            "https://bucket.s3.amazonaws.com/key.pdf",
            "https://objectstorage.region.oraclecloud.com/n/namespace/b/bucket/o/file.pdf",
            "http://localhost:8080/uploads/image/test.png"
    })
    @DisplayName("업로드 경로 및 https S3/OCI URL은 통과해야 한다")
    void shouldAcceptValidMediaUrls(String url) {
        assertThat(validator.isValid(url, mock(ConstraintValidatorContext.class))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "ftp://example.com/file.pdf",
            "uploads/foo",
            "not-a-url"
    })
    @DisplayName("허용되지 않는 URL 형식은 실패해야 한다")
    void shouldRejectInvalidUrls(String url) {
        assertThat(validator.isValid(url, mock(ConstraintValidatorContext.class))).isFalse();
    }

    @Test
    @DisplayName("null 또는 빈 문자열은 통과해야 한다")
    void shouldAcceptNullOrBlank() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("", context)).isTrue();
        assertThat(validator.isValid("   ", context)).isTrue();
    }
}
