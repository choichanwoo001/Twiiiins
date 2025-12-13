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

class ProjectUpdateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Title이 null이면 검증에 실패해야 한다")
    void shouldFailWhenTitleIsNull() {
        // given
        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setTitle(null); // Explicitly null

        // when
        Set<ConstraintViolation<ProjectUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title")
                && v.getMessage().contains("must not be blank") || v.getMessage().contains("공백일 수 없습니다"));
    }

    @Test
    @DisplayName("Title이 빈 문자열이면 검증에 실패해야 한다")
    void shouldFailWhenTitleIsBlank() {
        // given
        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setTitle("   ");

        // when
        Set<ConstraintViolation<ProjectUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("Title이 유효하면 검증에 성공해야 한다")
    void shouldSucceedWhenTitleIsValid() {
        // given
        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setTitle("Valid Title");

        // when
        Set<ConstraintViolation<ProjectUpdateRequest>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }
}
