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
}
