package com.nightchallenge.backend.mypage.dto.response;

import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;

import java.time.LocalDateTime;

/**
 * 용도: 각인 카드 목록 항목 응답.
 * 카드 모음 화면에 각인 기본 정보와 썸네일 렌더링용 최종 After 좌표만 전달한다.
 */
public record EngravingCardSummaryResponse(
        Long id,
        String constellationName,
        ConstellationShapeResponse constellationData,
        LocalDateTime createdAt
) {
}
