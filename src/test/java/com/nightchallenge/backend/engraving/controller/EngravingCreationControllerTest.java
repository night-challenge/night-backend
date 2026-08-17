package com.nightchallenge.backend.engraving.controller;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.service.EngravingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EngravingCreationControllerTest {

    private EngravingService engravingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        engravingService = mock(EngravingService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new EngravingCreationController(engravingService))
                .build();
    }

    @Test
    @DisplayName("각인을 처음 생성하면 201을 반환한다")
    void createNewEngravingReturnsCreated() throws Exception {
        NightPathRecord record = record(1L);
        given(engravingService.createFromGameSession(10L))
                .willReturn(new EngravingService.CreationResult(record, true));

        mockMvc.perform(post("/api/games/10/engravings"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("각인이 생성되었습니다."))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("동일 게임의 기존 각인을 반환하면 200과 같은 ID를 반환한다")
    void returnExistingEngravingReturnsOk() throws Exception {
        NightPathRecord record = record(1L);
        given(engravingService.createFromGameSession(10L))
                .willReturn(new EngravingService.CreationResult(record, false));

        mockMvc.perform(post("/api/games/10/engravings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    private NightPathRecord record(Long id) {
        ConstellationShape shape = new ConstellationShape(
                List.of(new ConstellationPoint(0, 1, 1)),
                List.of()
        );
        NightPathRecord record = new NightPathRecord(
                1L, 10L, "테스트 각인", List.of("도전"), "분석", new ConstellationData(shape, shape)
        );
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }
}
