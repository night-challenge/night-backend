package com.nightchallenge.backend.engraving.dto.response;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 용도: 각인 상세 응답.
 * 각인의 분석 정보와 원본 Before 및 최종 After 좌표 데이터를 상세 화면에 전달한다.
 */
public record EngravingDetailResponse(
        Long id,
        String constellationName,
        List<String> keywords,
        String comment,
        ConstellationDataResponse constellationData,
        LocalDateTime createdAt
) {

    /**
     * 용도: 각인 키워드 응답 데이터 보호.
     * 외부에서 전달한 키워드 목록을 복사해 DTO 생성 후 목록 자체가 변경되지 않게 한다.
     */
    public EngravingDetailResponse {
        keywords = List.copyOf(keywords);
    }

    /**
     * 용도: 각인 상세 DTO 변환.
     * NightPathRecord Entity를 API 명세의 camelCase 응답으로 변환한다.
     */
    public static EngravingDetailResponse from(NightPathRecord record) {
        return new EngravingDetailResponse(
                record.getId(),
                record.getConstellationName(),
                record.getKeywords(),
                record.getComment(),
                ConstellationDataResponse.from(record.getConstellationData()),
                record.getCreatedAt()
        );
    }
}