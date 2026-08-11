package com.nightchallenge.backend.engraving.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: 각인 이름 수정 요청 검증 테스트.
 * 별자리 이름 전달 여부를 Bean Validation 규칙에 따라 검증한다.
 */
class EngravingNameUpdateRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    /**
     * 용도: 요청 검증기 생성.
     * 테스트 전체에서 재사용할 Bean Validation 검증기를 준비한다.
     */
    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    /**
     * 용도: 요청 검증기 자원 정리.
     * 테스트 종료 후 ValidatorFactory가 사용하는 자원을 해제한다.
     */
    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("별자리 이름이 전달되면 검증을 통과한다")
    void acceptsProvidedConstellationName() {
        EngravingNameUpdateRequest request = new EngravingNameUpdateRequest("설렘의 흔적");

        Set<ConstraintViolation<EngravingNameUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("별자리 이름 필드가 누락되면 검증에 실패한다")
    void rejectsMissingConstellationName() {
        EngravingNameUpdateRequest request = new EngravingNameUpdateRequest(null);

        Set<ConstraintViolation<EngravingNameUpdateRequest>> violations = validator.validate(request);

        assertThat(violations).singleElement()
                .satisfies(violation -> {
                    assertThat(violation.getPropertyPath().toString()).isEqualTo("constellationName");
                    assertThat(violation.getMessage()).isEqualTo("별자리 이름을 입력해 주세요.");
                });
    }
}
