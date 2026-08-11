package com.nightchallenge.backend.mypage.dto.response;

/**
 * 용도: 최근 카드 요약 응답.
 * 마이페이지 메인 화면에 가장 최근에 생성된 카드의 식별자와 별자리 이름을 전달한다.
 */
public record RecentCardResponse(
        Long id,
        String constellationName
) {
}
