package com.nightchallenge.backend.engravingrequest.controller;

import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import com.nightchallenge.backend.engravingrequest.dto.request.EngravingRequestCreateRequest;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestCreateResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestListResponse;
import com.nightchallenge.backend.engravingrequest.service.EngravingRequestService;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import com.nightchallenge.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 용도: 제품 각인 신청 HTTP 요청 처리.
 * 신청 생성, 상태별 목록 조회와 신청 취소 요청을 Service에 전달하고 공통 응답으로 반환한다.
 */
@RestController
@RequestMapping("/api/engraving-requests")
@RequiredArgsConstructor
public class EngravingRequestController {

    private static final String CREATE_SUCCESS_MESSAGE = "신청이 완료되었습니다.";
    private static final String CANCEL_SUCCESS_MESSAGE = "신청 각인이 취소되었습니다.";

    private final EngravingRequestService engravingRequestService;

    /**
     * 용도: 제품 각인 신청 생성 API.
     * 요청받은 제품 옵션·보유 각인·색상으로 신청을 생성하고 201 응답을 반환한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EngravingRequestCreateResponse>> createEngravingRequest(
            @RequestBody EngravingRequestCreateRequest request
    ) {
        EngravingRequestCreateResponse response = engravingRequestService.createEngravingRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(CREATE_SUCCESS_MESSAGE, response));
    }

    /**
     * 용도: 상태별 제품 각인 신청 목록 API.
     * 신청 상태 문자열을 검증하고 해당 상태의 사용자 신청 목록을 조회한다.
     */
    @GetMapping
    public ApiResponse<EngravingRequestListResponse> getEngravingRequests(
            @RequestParam(required = false) String status
    ) {
        EngravingRequestStatus requestStatus = parseStatus(status);
        return ApiResponse.success(engravingRequestService.getEngravingRequests(requestStatus));
    }

    /**
     * 용도: 제품 각인 신청 취소 API.
     * URL 경로의 신청을 취소하고 명세의 성공 메시지와 null 데이터를 반환한다.
     */
    @PatchMapping("/{id}/cancel")
    public ApiResponse<Void> cancelEngravingRequest(@PathVariable Long id) {
        engravingRequestService.cancelEngravingRequest(id);
        return ApiResponse.success(CANCEL_SUCCESS_MESSAGE, null);
    }

    /**
     * 용도: 신청 상태 파라미터 검증.
     * 명세의 신청완료·취소됨 문자열만 enum으로 변환하고 그 외 값은 400 오류로 처리한다.
     */
    private EngravingRequestStatus parseStatus(String status) {
        try {
            return EngravingRequestStatus.fromValue(status);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.ENGRAVING_REQUEST_STATUS_INVALID);
        }
    }
}
