package com.nightchallenge.backend.mypage.controller;

import com.nightchallenge.backend.global.response.ApiResponse;
import com.nightchallenge.backend.mypage.dto.response.EngravingCardListResponse;
import com.nightchallenge.backend.mypage.dto.response.MyPageMainResponse;
import com.nightchallenge.backend.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용도: 마이페이지 HTTP 요청 처리.
 * 마이페이지 메인과 각인 카드 모음 조회 요청을 Service에 전달하고 공통 응답으로 반환한다.
 */
@RestController
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /**
     * 용도: 마이페이지 메인 조회 API.
     * 사용자 표시 정보와 신청 여부 및 최근 카드를 조회해 메시지 없는 공통 성공 응답으로 반환한다.
     */
    @GetMapping("/api/mypage")
    public ApiResponse<MyPageMainResponse> getMyPageMain() {
        return ApiResponse.success(myPageService.getMyPageMain());
    }

    /**
     * 용도: 각인 카드 모음 조회 API.
     * 사용자의 카드 목록을 최신순으로 조회해 메시지 없는 공통 성공 응답으로 반환한다.
     */
    @GetMapping("/api/engravings/cards")
    public ApiResponse<EngravingCardListResponse> getEngravingCards() {
        return ApiResponse.success(myPageService.getEngravingCards());
    }
}
