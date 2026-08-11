package com.nightchallenge.backend.engravingrequest.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 용도: 각인 신청 enum 변환 테스트.
 * 색상과 신청 상태가 API 명세의 문자열로 직렬화되고 다시 enum으로 변환되는지 검증한다.
 */
class EngravingRequestEnumTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("각인 색상은 명세의 소문자 문자열과 서로 변환된다")
    void convertsEngravingColorValues() throws Exception {
        assertThat(EngravingColor.fromValue("gold")).isEqualTo(EngravingColor.GOLD);
        assertThat(objectMapper.writeValueAsString(EngravingColor.SILVER)).isEqualTo("\"silver\"");
        assertThat(objectMapper.readValue("\"black\"", EngravingColor.class))
                .isEqualTo(EngravingColor.BLACK);
    }

    @Test
    @DisplayName("신청 상태는 명세의 한글 문자열과 서로 변환된다")
    void convertsEngravingRequestStatusValues() throws Exception {
        assertThat(EngravingRequestStatus.fromValue("신청완료"))
                .isEqualTo(EngravingRequestStatus.COMPLETED);
        assertThat(objectMapper.writeValueAsString(EngravingRequestStatus.CANCELED))
                .isEqualTo("\"취소됨\"");
        assertThat(objectMapper.readValue("\"신청완료\"", EngravingRequestStatus.class))
                .isEqualTo(EngravingRequestStatus.COMPLETED);
    }

    @Test
    @DisplayName("명세에 없는 색상과 신청 상태는 변환하지 않는다")
    void rejectsUnsupportedEnumValues() {
        assertThatThrownBy(() -> EngravingColor.fromValue("blue"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EngravingRequestStatus.fromValue("처리중"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
