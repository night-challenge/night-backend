package com.nightchallenge.backend.engravingrequest.dto.response;

import com.nightchallenge.backend.engraving.dto.response.EngravingSummaryResponse;

/**
 * 용도: 각인 신청 목록 항목 응답.
 * 신청 한 건의 제품 코드, 제품 옵션과 썸네일용 최종 After 각인 정보를 함께 전달한다.
 */
public record EngravingRequestSummaryResponse(
        Long id,
        String productCode,
        EngravingRequestProductResponse product,
        EngravingSummaryResponse engraving
) {
}
