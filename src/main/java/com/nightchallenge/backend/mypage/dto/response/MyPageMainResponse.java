package com.nightchallenge.backend.mypage.dto.response;

/**
 * 용도: 마이페이지 메인 응답.
 * 사용자 표시 정보와 신청 여부, 최근 카드 정보를 마이페이지 메인 화면에 전달한다.
 */
public record MyPageMainResponse(
        String nickname,
        String userIdDisplay,
        boolean hasEngravingRequest,
        RecentCardResponse recentCard
) {
}
