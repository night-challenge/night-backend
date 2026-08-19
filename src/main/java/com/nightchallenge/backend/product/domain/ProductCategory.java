package com.nightchallenge.backend.product.domain;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 용도: 제품 카테고리 구분.
 * 제품 목록 조회와 DB 저장에서 동일한 카테고리 값만 사용하도록 지원한다.
 */
public enum ProductCategory {

    BAG("가방"),
    TRAVEL("트래블"),
    FASHION_ACCESSORY("패션소품"),
    LIFESTYLE("라이프스타일");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 용도: 카테고리 표시명 조회.
     * API 명세와 화면에서 사용하는 한글 카테고리명을 반환한다.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 용도: 한글 카테고리 변환.
     * API에서 전달된 한글 카테고리명과 일치하는 enum 값을 찾아 반환한다.
     */
    public static Optional<ProductCategory> fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(category -> Objects.equals(category.displayName, displayName))
                .findFirst();
    }
}
