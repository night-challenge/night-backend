package com.nightchallenge.backend.engraving.controller;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.dto.response.EngravingDetailResponse;
import com.nightchallenge.backend.engraving.service.EngravingService;
import com.nightchallenge.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용도: 각인(별자리) 생성 HTTP 요청 처리.
 * 승리한 게임 세션으로부터 각인을 생성하는 요청을 Service에 전달하고 공통 ApiResponse 형식으로 반환한다.
 * 보유 각인 목록/상세 조회, 이름 수정 등 이미 생성된 각인을 다루는 API는 별도 컨트롤러(B파트)에서 담당한다.
 */
@RestController
@RequestMapping("/api/games/{gameSessionId}/engravings")
@RequiredArgsConstructor
public class EngravingCreationController {

    private final EngravingService engravingService;

    /**
     * 용도: 각인 생성 API.
     * 승리한 게임 세션의 나이트 이동 궤적을 별자리로 재구성하고 플레이 분석 결과와 함께 각인을 생성한다.
     */
    @PostMapping
    public ApiResponse<EngravingDetailResponse> createEngraving(
            @PathVariable Long gameSessionId
    ) {
        NightPathRecord record = engravingService.createFromGameSession(gameSessionId);
        return ApiResponse.success("각인이 생성되었습니다.", EngravingDetailResponse.from(record));
    }
}