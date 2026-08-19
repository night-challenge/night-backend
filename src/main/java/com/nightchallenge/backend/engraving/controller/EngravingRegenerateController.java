package com.nightchallenge.backend.engraving.controller;

import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.dto.response.EngravingDetailResponse;
import com.nightchallenge.backend.engraving.service.EngravingService;
import com.nightchallenge.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용도: 각인 별자리 재생성 HTTP 요청 처리.
 * 화면 5.1의 [다시 생성하기] 요청을 Service에 전달하고 공통 ApiResponse 형식으로 반환한다.
 * 보유 각인 목록/상세 조회, 이름 수정 등 다른 각인 API는 별도 컨트롤러(B파트)에서 담당하므로,
 * 이 컨트롤러는 재생성 경로(/regenerate)만 다룬다.
 */
@RestController
@RequestMapping("/api/engravings")
@RequiredArgsConstructor
public class EngravingRegenerateController {

    private final EngravingService engravingService;

    /**
     * 용도: 각인 별자리 재생성 API.
     * 원본 이동 궤적(before)은 유지한 채 최종 별자리(after)만 새로운 모양으로 다시 생성한다.
     */
    @PatchMapping("/{id}/regenerate")
    public ApiResponse<EngravingDetailResponse> regenerate(
            @PathVariable Long id
    ) {
        NightPathRecord record = engravingService.regenerate(id);
        return ApiResponse.success("별자리가 다시 생성되었습니다.", EngravingDetailResponse.from(record));
    }
}