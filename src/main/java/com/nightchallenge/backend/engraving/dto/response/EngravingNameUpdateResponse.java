package com.nightchallenge.backend.engraving.dto.response;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;

/**
 * 용도: 각인 이름 수정 응답.
 * 수정된 각인의 식별자와 별자리 이름을 이름 수정 성공 결과로 전달한다.
 */
public record EngravingNameUpdateResponse(
        Long id,
        String constellationName
) {

    /**
     * 용도: 각인 이름 수정 DTO 변환.
     * 이름 변경이 반영된 각인의 식별자와 별자리 이름을 성공 응답으로 변환한다.
     */
    public static EngravingNameUpdateResponse from(NightPathRecord record) {
        return new EngravingNameUpdateResponse(record.getId(), record.getConstellationName());
    }
}
