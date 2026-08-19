package com.nightchallenge.backend.engravingrequest.controller;

import com.nightchallenge.backend.engraving.dto.response.ConstellationPointResponse;
import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingSummaryResponse;
import com.nightchallenge.backend.engravingrequest.domain.EngravingColor;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestCreateResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestListResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestProductResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestSummaryResponse;
import com.nightchallenge.backend.engravingrequest.service.EngravingRequestService;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import com.nightchallenge.backend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 용도: 제품 각인 신청 Controller HTTP 응답 검증.
 * 신청 생성·목록·취소 URL과 공통 성공·오류 응답 및 상태 파라미터 검증을 확인한다.
 */
class EngravingRequestControllerTest {

    private EngravingRequestService engravingRequestService;
    private MockMvc mockMvc;

    /**
     * 용도: Controller 테스트 환경 구성.
     * Mock Service와 전역 예외 처리기를 MockMvc에 연결한다.
     */
    @BeforeEach
    void setUp() {
        engravingRequestService = mock(EngravingRequestService.class);
        EngravingRequestController controller = new EngravingRequestController(engravingRequestService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("제품 각인 신청을 생성하고 공통 201 응답을 반환한다")
    void createEngravingRequest() throws Exception {
        given(engravingRequestService.createEngravingRequest(any()))
                .willReturn(new EngravingRequestCreateResponse(5L, "NWdfw25"));

        mockMvc.perform(post("/api/engraving-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nightPathRecordId": 1,
                                  "productOptionId": 3,
                                  "engravingColor": "gold"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("신청이 완료되었습니다."))
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.productCode").value("NWdfw25"));
    }

    @Test
    @DisplayName("잘못된 각인 색상 요청은 공통 400 오류 응답으로 반환한다")
    void createWithInvalidColor() throws Exception {
        mockMvc.perform(post("/api/engraving-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nightPathRecordId": 1,
                                  "productOptionId": 3,
                                  "engravingColor": "GOLD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("신청 필수 선택값 누락 오류를 공통 400 응답으로 반환한다")
    void createWithMissingSelections() throws Exception {
        given(engravingRequestService.createEngravingRequest(any()))
                .willThrow(new BusinessException(ErrorCode.ENGRAVING_REQUEST_SELECTION_REQUIRED));

        mockMvc.perform(post("/api/engraving-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nightPathRecordId": null,
                                  "productOptionId": 3,
                                  "engravingColor": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("제품에 새길 각인과 각인 색상을 선택해 주세요."));
    }

    @Test
    @DisplayName("존재하지 않는 제품 옵션의 신청은 공통 404 오류 응답으로 반환한다")
    void createWithMissingProductOption() throws Exception {
        given(engravingRequestService.createEngravingRequest(any()))
                .willThrow(new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 제품입니다."));

        mockMvc.perform(post("/api/engraving-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nightPathRecordId": 1,
                                  "productOptionId": 999,
                                  "engravingColor": "black"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 제품입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("신청완료 상태의 목록을 공통 성공 응답으로 반환한다")
    void getEngravingRequests() throws Exception {
        given(engravingRequestService.getEngravingRequests(EngravingRequestStatus.COMPLETED))
                .willReturn(createListResponse());

        mockMvc.perform(get("/api/engraving-requests").queryParam("status", "신청완료"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.records[0].id").value(5))
                .andExpect(jsonPath("$.data.records[0].engravingColor").value("gold"))
                .andExpect(jsonPath("$.data.records[0].product.optionName")
                        .value("L 비세토스 수트케이스"))
                .andExpect(jsonPath("$.data.records[0].engraving.constellationData.points[0].id")
                        .value(10));
    }

    @Test
    @DisplayName("목록 결과가 없으면 records 빈 배열을 반환한다")
    void getEmptyEngravingRequests() throws Exception {
        given(engravingRequestService.getEngravingRequests(EngravingRequestStatus.CANCELED))
                .willReturn(new EngravingRequestListResponse(List.of()));

        mockMvc.perform(get("/api/engraving-requests").queryParam("status", "취소됨"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("누락된 신청 상태는 공통 400 오류 응답으로 반환한다")
    void getWithoutStatus() throws Exception {
        mockMvc.perform(get("/api/engraving-requests"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 신청 상태입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("잘못된 신청 상태는 공통 400 오류 응답으로 반환한다")
    void getWithInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/engraving-requests").queryParam("status", "COMPLETED"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 신청 상태입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("신청을 취소하고 공통 성공 응답을 반환한다")
    void cancelEngravingRequest() throws Exception {
        mockMvc.perform(patch("/api/engraving-requests/{id}/cancel", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("신청 각인이 취소되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(engravingRequestService).cancelEngravingRequest(5L);
    }

    @Test
    @DisplayName("존재하지 않는 신청 취소는 공통 404 오류 응답으로 반환한다")
    void cancelMissingEngravingRequest() throws Exception {
        doThrow(new BusinessException(ErrorCode.ENGRAVING_REQUEST_NOT_FOUND))
                .when(engravingRequestService).cancelEngravingRequest(999L);

        mockMvc.perform(patch("/api/engraving-requests/{id}/cancel", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 신청 건입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("이미 취소된 신청은 공통 400 오류 응답으로 반환한다")
    void cancelAlreadyCanceledEngravingRequest() throws Exception {
        doThrow(new BusinessException(ErrorCode.ENGRAVING_REQUEST_ALREADY_CANCELED))
                .when(engravingRequestService).cancelEngravingRequest(5L);

        mockMvc.perform(patch("/api/engraving-requests/{id}/cancel", 5L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이미 취소된 신청 건입니다."));
    }

    /**
     * 용도: 테스트용 신청 목록 응답 생성.
     * 목록 API의 중첩 제품·각인·After 응답 구조를 검증할 데이터를 구성한다.
     */
    private EngravingRequestListResponse createListResponse() {
        ConstellationShapeResponse after = new ConstellationShapeResponse(
                List.of(new ConstellationPointResponse(10, 40, 300)),
                List.of()
        );
        EngravingSummaryResponse engraving = new EngravingSummaryResponse(
                1L,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                after,
                LocalDateTime.of(2026, 8, 7, 10, 30)
        );
        EngravingRequestSummaryResponse record = new EngravingRequestSummaryResponse(
                5L,
                "NWdfw25",
                EngravingColor.GOLD,
                new EngravingRequestProductResponse("L 비세토스 수트케이스", "갈색"),
                engraving
        );
        return new EngravingRequestListResponse(List.of(record));
    }
}
