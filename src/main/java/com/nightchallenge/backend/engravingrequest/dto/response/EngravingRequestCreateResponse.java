package com.nightchallenge.backend.engravingrequest.dto.response;

/**
 * 용도: 각인 신청 생성 응답.
 * 생성된 신청의 식별자와 제품 주문 시 사용할 고유 제품 코드를 반환한다.
 */
public record EngravingRequestCreateResponse(
        Long id,
        String productCode
) {
}
