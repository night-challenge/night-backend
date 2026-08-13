package com.nightchallenge.backend.engraving.controller;

import com.nightchallenge.backend.engraving.dto.request.EngravingNameUpdateRequest;
import com.nightchallenge.backend.engraving.dto.response.EngravingDetailResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingListResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingNameUpdateResponse;
import com.nightchallenge.backend.engraving.service.EngravingService;
import com.nightchallenge.backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용도: 보유 각인 HTTP 요청 처리.
 * 사용자가 보유한 각인의 목록·상세 조회와 이름 수정 요청을 Service에 전달하고 공통 ApiResponse 형식으로 반환한다.
 */
@RestController
@RequestMapping("/api/engravings")
@RequiredArgsConstructor
public class EngravingController {

    private final EngravingService engravingService;

    /**
     * 용도: 보유 각인 목록 조회 API.
     * 고정 사용자의 각인 목록을 최신순으로 조회해 별도의 안내 문구 없이 반환한다.
     */
    @GetMapping
    public ApiResponse<EngravingListResponse> getEngravings() {
        return ApiResponse.success(engravingService.getEngravings());
    }

    /**
     * 용도: 보유 각인 상세 조회 API.
     * 각인 식별자에 해당하는 상세 정보와 before·after 별자리 데이터를 반환한다.
     */
    @GetMapping("/{id}")
    public ApiResponse<EngravingDetailResponse> getEngraving(@PathVariable Long id) {
        return ApiResponse.success(engravingService.getEngraving(id));
    }

    /**
     * 용도: 보유 각인 이름 수정 API.
     * 요청값을 검증한 뒤 각인 이름을 변경하고 수정 완료 메시지와 결과를 반환한다.
     */
    @PatchMapping("/{id}")
    public ApiResponse<EngravingNameUpdateResponse> updateEngravingName(
            @PathVariable Long id,
            @Valid @RequestBody EngravingNameUpdateRequest request
    ) {
        EngravingNameUpdateResponse response = engravingService.updateEngravingName(
                id,
                request.constellationName()
        );
        return ApiResponse.success("수정되었습니다.", response);
    }
}
