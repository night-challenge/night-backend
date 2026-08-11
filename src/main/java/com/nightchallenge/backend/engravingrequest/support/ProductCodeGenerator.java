package com.nightchallenge.backend.engravingrequest.support;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

/**
 * 용도: 제품 코드 생성.
 * 각인 신청마다 영문 대문자·소문자·숫자로 구성된 7자리 제품 코드를 생성한다.
 * 중복 확인 함수를 전달받아 최대 10회까지 새로운 코드를 생성할 수 있도록 한다.
 */
@Component
public class ProductCodeGenerator {

    private static final String CODE_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final IntUnaryOperator randomIndexGenerator;

    /**
     * 용도: 난수 생성기 초기화.
     * 예측하기 어려운 제품 코드를 만들기 위해 SecureRandom을 사용한다.
     */
    public ProductCodeGenerator() {
        SecureRandom secureRandom = new SecureRandom();
        this.randomIndexGenerator = secureRandom::nextInt;
    }

    ProductCodeGenerator(IntUnaryOperator randomIndexGenerator) {
        this.randomIndexGenerator = Objects.requireNonNull(randomIndexGenerator);
    }

    /**
     * 용도: 제품 코드 단건 생성.
     * 허용된 문자 집합에서 문자를 무작위로 선택하여 7자리 코드를 만든다.
     */
    public String generate() {
        StringBuilder productCode = new StringBuilder(CODE_LENGTH);

        for (int index = 0; index < CODE_LENGTH; index++) {
            int characterIndex = randomIndexGenerator.applyAsInt(CODE_CHARACTERS.length());
            productCode.append(CODE_CHARACTERS.charAt(characterIndex));
        }

        return productCode.toString();
    }

    /**
     * 용도: 고유 제품 코드 생성.
     * 전달받은 중복 확인 함수로 생성 결과를 검사하고 중복이면 최대 10회까지 다시 생성한다.
     */
    public String generateUniqueCode(Predicate<String> duplicateChecker) {
        Objects.requireNonNull(duplicateChecker);

        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String productCode = generate();
            if (!duplicateChecker.test(productCode)) {
                return productCode;
            }
        }

        throw new IllegalStateException("고유한 제품 코드를 생성하지 못했습니다.");
    }
}
