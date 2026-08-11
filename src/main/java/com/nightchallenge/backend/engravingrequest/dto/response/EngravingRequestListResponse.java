package com.nightchallenge.backend.engravingrequest.dto.response;

import java.util.List;

/**
 * 용도: 각인 신청 목록 응답.
 * 요청한 신청 상태에 해당하는 모든 신청 건을 records 배열로 전달한다.
 */
public record EngravingRequestListResponse(
        List<EngravingRequestSummaryResponse> records
) {

    /**
     * 용도: 신청 목록 응답 데이터 보호.
     * 외부 목록 변경이 응답 객체에 영향을 주지 않도록 복사하여 저장한다.
     */
    public EngravingRequestListResponse {
        records = List.copyOf(records);
    }
}
