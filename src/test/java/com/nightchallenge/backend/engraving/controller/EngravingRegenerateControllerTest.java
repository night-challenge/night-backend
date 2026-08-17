package com.nightchallenge.backend.engraving.controller;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.service.EngravingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EngravingRegenerateControllerTest {

    @Test
    @DisplayName("각인 재생성은 기존대로 200을 반환한다")
    void regenerateReturnsOk() throws Exception {
        EngravingService service = mock(EngravingService.class);
        ConstellationShape shape = new ConstellationShape(
                List.of(new ConstellationPoint(0, 1, 1)),
                List.of()
        );
        NightPathRecord record = new NightPathRecord(
                1L, 10L, "테스트 각인", List.of("도전"), "분석", new ConstellationData(shape, shape)
        );
        ReflectionTestUtils.setField(record, "id", 1L);
        given(service.regenerate(1L)).willReturn(record);
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new EngravingRegenerateController(service))
                .build();

        mockMvc.perform(patch("/api/engravings/1/regenerate"))
                .andExpect(status().isOk());
    }
}
