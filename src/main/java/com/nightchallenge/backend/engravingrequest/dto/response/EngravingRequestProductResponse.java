package com.nightchallenge.backend.engravingrequest.dto.response;

/**
 * 용도: 각인 신청 제품 응답.
 * 신청 목록 화면에 필요한 제품명과 색상 또는 용량 등의 옵션 구분값만 전달한다.
 */
public record EngravingRequestProductResponse(
        String optionName,
        String optionLabel
) {
}
