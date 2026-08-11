package com.nightchallenge.backend.engraving.dto.response;

import java.util.List;

/**
 * 용도: 각인 목록 응답.
 * 사용자의 각인들을 records 배열로 감싸며 조회 결과가 없을 때도 빈 배열을 유지한다.
 */
public record EngravingListResponse(List<EngravingSummaryResponse> records) {

    /**
     * 용도: 각인 목록 응답 데이터 보호.
     * 외부에서 전달한 각인 목록을 복사해 DTO 생성 후 목록 자체가 변경되지 않게 한다.
     */
    public EngravingListResponse {
        records = List.copyOf(records);
    }
}
