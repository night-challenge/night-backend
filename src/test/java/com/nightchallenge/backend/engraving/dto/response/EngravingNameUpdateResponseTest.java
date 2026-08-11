package com.nightchallenge.backend.engraving.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: 각인 이름 수정 응답 테스트.
 * 수정 결과 DTO가 명세의 id와 constellationName 필드를 그대로 전달하는지 검증한다.
 */
class EngravingNameUpdateResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("수정된 각인 ID와 별자리 이름을 응답한다")
    void createsEngravingNameUpdateResponse() throws Exception {
        EngravingNameUpdateResponse response = new EngravingNameUpdateResponse(1L, "설렘의 흔적");

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.constellationName()).isEqualTo("설렘의 흔적");
        assertThat(json.get("id").asLong()).isEqualTo(1L);
        assertThat(json.get("constellationName").asString()).isEqualTo("설렘의 흔적");
    }
}
