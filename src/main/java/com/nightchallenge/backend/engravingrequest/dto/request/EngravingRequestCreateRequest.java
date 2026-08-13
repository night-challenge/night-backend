package com.nightchallenge.backend.engravingrequest.dto.request;

import com.nightchallenge.backend.engravingrequest.domain.EngravingColor;

/**
 * 용도: 각인 신청 생성 요청.
 * 신청에 사용할 보유 각인, 제품 옵션과 각인 색상을 API 명세의 camelCase 구조로 전달한다.
 * 각인과 색상의 누락 조합별 메시지는 Service에서 함께 판단하므로 필드 단위 검증은 적용하지 않는다.
 */
public record EngravingRequestCreateRequest(
        Long nightPathRecordId,
        Long productOptionId,
        EngravingColor engravingColor
) {
}
