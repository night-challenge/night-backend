package com.nightchallenge.backend.engraving.dto.response;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 용도: 각인 목록 항목 응답.
 * 목록 화면에 필요한 각인 정보와 썸네일 렌더링용 최종 After 데이터만 전달한다.
 */
public record EngravingSummaryResponse(
        Long id,
        String constellationName,
        List<String> keywords,
        String comment,
        ConstellationShapeResponse constellationData,
        LocalDateTime createdAt
) {

    /**
     * 용도: 각인 키워드 응답 데이터 보호.
     * 외부에서 전달한 키워드 목록을 복사해 DTO 생성 후 목록 자체가 변경되지 않게 한다.
     */
    public EngravingSummaryResponse {
        keywords = List.copyOf(keywords);
    }

    /**
     * 용도: 각인 목록 DTO 변환.
     * NightPathRecord에서 목록 화면에 필요한 정보와 최종 after 별자리만 추출한다.
     */
    public static EngravingSummaryResponse from(NightPathRecord record) {
        return new EngravingSummaryResponse(
                record.getId(),
                record.getConstellationName(),
                record.getKeywords(),
                record.getComment(),
                ConstellationShapeResponse.from(record.getConstellationData().after()),
                record.getCreatedAt()
        );
    }
}
