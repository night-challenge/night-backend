package com.nightchallenge.backend.product.dto.response;

import com.nightchallenge.backend.product.domain.ProductOption;

/**
 * 용도: 제품 옵션 목록 항목 응답.
 * 목록 화면에 필요한 옵션 식별자, 제품명, 구분값과 가격만 전달한다.
 */
public record ProductOptionSummaryResponse(
        Long id,
        String optionName,
        String optionLabel,
        int price
) {

    /**
     * 용도: 제품 옵션 목록 DTO 변환.
     * 영속성 Entity를 외부 API에 노출하지 않고 명세의 camelCase 응답으로 변환한다.
     */
    public static ProductOptionSummaryResponse from(ProductOption option) {
        return new ProductOptionSummaryResponse(
                option.getId(),
                option.getOptionName(),
                option.getOptionLabel(),
                option.getPrice()
        );
    }
}
