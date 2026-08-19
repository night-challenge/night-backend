package com.nightchallenge.backend.product.dto.response;

import com.nightchallenge.backend.product.domain.ProductOption;

import java.util.List;

/**
 * 용도: 카테고리별 제품 옵션 목록 응답.
 * 조회한 옵션들을 options 배열로 감싸며 결과가 없을 때도 빈 배열을 유지한다.
 */
public record ProductOptionListResponse(List<ProductOptionSummaryResponse> options) {

    public ProductOptionListResponse {
        options = List.copyOf(options);
    }

    /**
     * 용도: 제품 옵션 목록 응답 변환.
     * Entity 목록의 각 항목을 목록 DTO로 변환해 API 명세의 options 배열을 구성한다.
     */
    public static ProductOptionListResponse from(List<ProductOption> options) {
        List<ProductOptionSummaryResponse> responses = options.stream()
                .map(ProductOptionSummaryResponse::from)
                .toList();

        return new ProductOptionListResponse(responses);
    }
}
