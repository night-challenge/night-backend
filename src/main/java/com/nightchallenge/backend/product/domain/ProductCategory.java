package com.nightchallenge.backend.product.domain;

/**
 * 용도: 제품 카테고리 구분
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
     * 용도: 카테고리 표시명 조회
     * API 명세와 화면에서 사용하는 한글 카테고리명을 반환한다.
     */
    public String getDisplayName() {
        return displayName;
    }
}
