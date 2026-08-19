package com.nightchallenge.backend.mypage.controller;

import com.nightchallenge.backend.engraving.dto.response.ConstellationPointResponse;
import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;
import com.nightchallenge.backend.mypage.dto.response.EngravingCardListResponse;
import com.nightchallenge.backend.mypage.dto.response.EngravingCardSummaryResponse;
import com.nightchallenge.backend.mypage.dto.response.MyPageMainResponse;
import com.nightchallenge.backend.mypage.dto.response.RecentCardResponse;
import com.nightchallenge.backend.mypage.service.MyPageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 용도: 마이페이지 Controller HTTP 응답 검증.
 * 마이페이지 메인과 카드 모음 URL 및 공통 성공 응답과 빈 데이터 구조를 검증한다.
 */
class MyPageControllerTest {

    private MyPageService myPageService;
    private MockMvc mockMvc;

    /**
     * 용도: Controller 테스트 환경 구성.
     * Mock Service를 사용하는 마이페이지 Controller를 MockMvc에 연결한다.
     */
    @BeforeEach
    void setUp() {
        myPageService = mock(MyPageService.class);
        MyPageController controller = new MyPageController(myPageService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("마이페이지 메인 정보를 공통 성공 응답으로 반환한다")
    void getMyPageMain() throws Exception {
        given(myPageService.getMyPageMain()).willReturn(new MyPageMainResponse(
                "사자후",
                "sajahoo",
                true,
                new RecentCardResponse(2L, "설렘의 흔적")
        ));

        mockMvc.perform(get("/api/mypage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.nickname").value("사자후"))
                .andExpect(jsonPath("$.data.userIdDisplay").value("sajahoo"))
                .andExpect(jsonPath("$.data.hasEngravingRequest").value(true))
                .andExpect(jsonPath("$.data.recentCard.id").value(2))
                .andExpect(jsonPath("$.data.recentCard.constellationName").value("설렘의 흔적"));

        verify(myPageService).getMyPageMain();
    }

    @Test
    @DisplayName("최근 카드가 없으면 recentCard를 null로 반환한다")
    void getMyPageMainWithoutRecentCard() throws Exception {
        given(myPageService.getMyPageMain()).willReturn(new MyPageMainResponse(
                "사자후",
                "sajahoo",
                false,
                null
        ));

        mockMvc.perform(get("/api/mypage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.hasEngravingRequest").value(false))
                .andExpect(jsonPath("$.data.recentCard").isEmpty());
    }

    @Test
    @DisplayName("각인 카드 모음을 최종 After 데이터와 함께 반환한다")
    void getEngravingCards() throws Exception {
        ConstellationShapeResponse after = new ConstellationShapeResponse(
                List.of(
                        new ConstellationPointResponse(10, 40, 250),
                        new ConstellationPointResponse(11, 120, 180)
                ),
                List.of(List.of(10, 11))
        );
        EngravingCardSummaryResponse card = new EngravingCardSummaryResponse(
                2L,
                "설렘의 흔적",
                after,
                LocalDateTime.of(2026, 8, 8, 14, 20)
        );
        given(myPageService.getEngravingCards())
                .willReturn(new EngravingCardListResponse(List.of(card)));

        mockMvc.perform(get("/api/engravings/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.cards[0].id").value(2))
                .andExpect(jsonPath("$.data.cards[0].constellationName").value("설렘의 흔적"))
                .andExpect(jsonPath("$.data.cards[0].constellationData.points[0].id").value(10))
                .andExpect(jsonPath("$.data.cards[0].constellationData.connections[0][0]").value(10))
                .andExpect(jsonPath("$.data.cards[0].createdAt").value("2026-08-08T14:20:00"));

        verify(myPageService).getEngravingCards();
    }

    @Test
    @DisplayName("보유 카드가 없으면 cards 빈 배열을 반환한다")
    void getEmptyEngravingCards() throws Exception {
        given(myPageService.getEngravingCards())
                .willReturn(new EngravingCardListResponse(List.of()));

        mockMvc.perform(get("/api/engravings/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.cards").isArray())
                .andExpect(jsonPath("$.data.cards").isEmpty());
    }
}
