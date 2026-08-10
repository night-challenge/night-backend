package com.nightchallenge.backend.product.dto.response;

import com.nightchallenge.backend.product.domain.ProductOption;

/**
 * 용도: 제품 옵션 상세 응답.
 * 선택한 옵션의 기본 정보와 해당 제품의 공통 설명을 상세 화면에 전달한다.
 */
public record ProductOptionDetailResponse(
        Long id,
        String optionName,
        String optionLabel,
        int price,
        String description
) {

    /**
     * 용도: 제품 옵션 상세 DTO 변환.
     * 옵션 정보와 연관된 제품 설명을 조합해 API 명세의 상세 응답으로 변환한다.
     */
    public static ProductOptionDetailResponse from(ProductOption option) {
        return new ProductOptionDetailResponse(
                option.getId(),
                option.getOptionName(),
                option.getOptionLabel(),
                option.getPrice(),
                option.getProduct().getDescription()
        );
    }
}
