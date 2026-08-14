package com.nightchallenge.backend.engravingrequest.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 용도: 제품 코드 생성 규칙 테스트.
 * 생성된 코드의 길이와 문자 범위 및 최대 10회 중복 재시도 동작을 검증한다.
 */
class ProductCodeGeneratorTest {

    private final ProductCodeGenerator productCodeGenerator = new ProductCodeGenerator();

    @Test
    @DisplayName("제품 코드는 영문 대문자·소문자·숫자로 구성된 7자리이다")
    void generatesSevenCharacterAlphanumericCode() {
        String productCode = productCodeGenerator.generate();

        assertThat(productCode).matches("^[A-Za-z0-9]{7}$");
    }

    @Test
    @DisplayName("제품 코드를 반복 생성해도 모든 결과가 생성 규칙을 만족한다")
    void repeatedlyGeneratesValidProductCodes() {
        for (int count = 0; count < 1_000; count++) {
            assertThat(productCodeGenerator.generate()).matches("^[A-Za-z0-9]{7}$");
        }
    }

    @Test
    @DisplayName("중복 코드가 생성되면 최대 10회 범위에서 다시 생성한다")
    void retriesWhenGeneratedCodeAlreadyExists() {
        AtomicInteger duplicateChecks = new AtomicInteger();

        String productCode = productCodeGenerator.generateUniqueCode(
                code -> duplicateChecks.incrementAndGet() < 10
        );

        assertThat(productCode).matches("^[A-Za-z0-9]{7}$");
        assertThat(duplicateChecks).hasValue(10);
    }

    @Test
    @DisplayName("10회 모두 중복이면 제품 코드 생성을 실패한다")
    void failsAfterTenDuplicateCodes() {
        AtomicInteger duplicateChecks = new AtomicInteger();

        assertThatThrownBy(() -> productCodeGenerator.generateUniqueCode(code -> {
            duplicateChecks.incrementAndGet();
            return true;
        }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("고유한 제품 코드를 생성하지 못했습니다.");
        assertThat(duplicateChecks).hasValue(10);
    }
}
