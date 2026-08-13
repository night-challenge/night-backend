package com.nightchallenge.backend.engraving.controller;

import com.nightchallenge.backend.engraving.dto.response.ConstellationDataResponse;
import com.nightchallenge.backend.engraving.dto.response.ConstellationPointResponse;
import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingDetailResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingListResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingNameUpdateResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingSummaryResponse;
import com.nightchallenge.backend.engraving.service.EngravingService;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import com.nightchallenge.backend.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 용도: 보유 각인 Controller HTTP 응답 검증.
 * Mock Service와 전역 예외 처리기를 연결해 URL, 공통 응답, 요청값 검증과 400·404 오류를 검증한다.
 */
class EngravingControllerTest {

    private EngravingService engravingService;
    private MockMvc mockMvc;

    /**
     * 용도: Controller 테스트 환경 구성.
     * Mock Service, Bean Validation과 전역 예외 처리기를 MockMvc에 연결한다.
     */
    @BeforeEach
    void setUp() {
        engravingService = mock(EngravingService.class);
        EngravingController engravingController = new EngravingController(engravingService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(engravingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    @DisplayName("보유 각인 목록을 공통 성공 응답으로 반환한다")
    void getEngravings() throws Exception {
        EngravingSummaryResponse summary = new EngravingSummaryResponse(
                1L,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                afterShape(),
                LocalDateTime.of(2026, 8, 7, 10, 30)
        );
        given(engravingService.getEngravings())
                .willReturn(new EngravingListResponse(List.of(summary)));

        mockMvc.perform(get("/api/engravings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].constellationData.points[0].id").value(10))
                .andExpect(jsonPath("$.data.records[0].constellationData.before").doesNotExist());
    }

    @Test
    @DisplayName("보유 각인이 없으면 records 빈 배열을 반환한다")
    void getEngravingsReturnsEmptyRecords() throws Exception {
        given(engravingService.getEngravings())
                .willReturn(new EngravingListResponse(List.of()));

        mockMvc.perform(get("/api/engravings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("보유 각인 상세 정보를 before와 after를 포함해 반환한다")
    void getEngraving() throws Exception {
        EngravingDetailResponse detail = new EngravingDetailResponse(
                1L,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                new ConstellationDataResponse(beforeShape(), afterShape()),
                LocalDateTime.of(2026, 8, 7, 10, 30)
        );
        given(engravingService.getEngraving(1L)).willReturn(detail);

        mockMvc.perform(get("/api/engravings/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.constellationData.before.points[0].id").value(0))
                .andExpect(jsonPath("$.data.constellationData.after.points[0].id").value(10));
    }

    @Test
    @DisplayName("존재하지 않는 각인 상세 조회는 공통 404 오류 응답을 반환한다")
    void getEngravingNotFound() throws Exception {
        given(engravingService.getEngraving(999L))
                .willThrow(new BusinessException(ErrorCode.ENGRAVING_NOT_FOUND));

        mockMvc.perform(get("/api/engravings/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 각인입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("각인 이름 수정 결과를 성공 메시지와 함께 반환한다")
    void updateEngravingName() throws Exception {
        given(engravingService.updateEngravingName(1L, "설렘의 흔적"))
                .willReturn(new EngravingNameUpdateResponse(1L, "설렘의 흔적"));

        mockMvc.perform(patch("/api/engravings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"constellationName\":\"설렘의 흔적\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("수정되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.constellationName").value("설렘의 흔적"));
    }

    @Test
    @DisplayName("각인 이름 필드가 누락되면 공통 400 검증 오류를 반환한다")
    void updateEngravingNameWithoutName() throws Exception {
        mockMvc.perform(patch("/api/engravings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("별자리 이름을 입력해 주세요."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("기존 이름과 동일하면 공통 400 오류 응답을 반환한다")
    void updateEngravingNameUnchanged() throws Exception {
        given(engravingService.updateEngravingName(1L, "오리온의 흔적"))
                .willThrow(new BusinessException(ErrorCode.ENGRAVING_NAME_UNCHANGED));

        mockMvc.perform(patch("/api/engravings/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"constellationName\":\"오리온의 흔적\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("변경된 사항이 없습니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 각인의 이름 수정은 공통 404 오류 응답을 반환한다")
    void updateEngravingNameNotFound() throws Exception {
        given(engravingService.updateEngravingName(999L, "새 이름"))
                .willThrow(new BusinessException(ErrorCode.ENGRAVING_NOT_FOUND));

        mockMvc.perform(patch("/api/engravings/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"constellationName\":\"새 이름\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("존재하지 않는 각인입니다."))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    /**
     * 용도: 테스트용 before 별자리 생성.
     * 상세 조회 응답에서 원본 격자 좌표가 포함되는지 검증할 데이터를 만든다.
     */
    private ConstellationShapeResponse beforeShape() {
        return new ConstellationShapeResponse(
                List.of(
                        new ConstellationPointResponse(0, 1, 3),
                        new ConstellationPointResponse(1, 3, 4)
                ),
                List.of(List.of(0, 1))
        );
    }

    /**
     * 용도: 테스트용 after 별자리 생성.
     * 목록과 상세 조회 응답에서 최종 캔버스 좌표가 포함되는지 검증할 데이터를 만든다.
     */
    private ConstellationShapeResponse afterShape() {
        return new ConstellationShapeResponse(
                List.of(
                        new ConstellationPointResponse(10, 40, 250),
                        new ConstellationPointResponse(11, 120, 180)
                ),
                List.of(List.of(10, 11))
        );
    }
}
