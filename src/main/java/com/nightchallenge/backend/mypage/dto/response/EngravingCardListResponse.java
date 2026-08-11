package com.nightchallenge.backend.mypage.dto.response;

import java.util.List;

/**
 * 용도: 각인 카드 목록 응답.
 * 사용자의 카드들을 cards 배열로 감싸며 조회 결과가 없을 때도 빈 배열을 유지한다.
 */
public record EngravingCardListResponse(List<EngravingCardSummaryResponse> cards) {

    /**
     * 용도: 카드 목록 응답 데이터 보호.
     * 외부에서 전달한 카드 목록을 복사해 DTO 생성 후 목록 자체가 변경되지 않게 한다.
     */
    public EngravingCardListResponse {
        cards = List.copyOf(cards);
    }
}
