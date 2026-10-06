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

class DownloadFileUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("수정 시 /uploads/ 경로 fileUrl은 검증에 성공해야 한다")
    void shouldAcceptUploadsPathOnUpdate() {
        DownloadFileUpdateRequest request = new DownloadFileUpdateRequest();
        request.setName("Updated name");
        request.setFileUrl("/uploads/file/existing.pdf");

        Set<ConstraintViolation<DownloadFileUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
    @Test
    void shouldValidateDropboxSourceAgainstHost() {
        DownloadFileUpdateRequest request = new DownloadFileUpdateRequest();
        request.setName("Press photos");
        request.setDownloadSource("dropbox");
        request.setFileUrl("https://dropbox.com.evil.test/s/photo.jpg");
        assertThat(validator.validate(request)).anyMatch(v -> v.getPropertyPath().toString().equals("downloadSourceValid"));
        request.setFileUrl("https://www.dropbox.com/scl/fo/press?dl=0");
        assertThat(validator.validate(request)).isEmpty();
        request.setDownloadSource("unknown");
        assertThat(validator.validate(request)).anyMatch(v -> v.getPropertyPath().toString().equals("downloadSource"));
    }
}
