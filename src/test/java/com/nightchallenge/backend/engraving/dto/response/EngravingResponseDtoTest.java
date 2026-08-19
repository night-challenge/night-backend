package com.nightchallenge.backend.engraving.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 용도: 각인 응답 DTO 구조 테스트.
 * 목록과 상세 응답의 Before 및 After 구성과 JSON 필드 형태를 명세 기준으로 검증한다.
 */
class EngravingResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("각인 목록은 records 배열과 최종 After 데이터만 포함한다")
    void createsEngravingListResponseWithAfterData() {
        ConstellationShapeResponse after = createAfterShape();
        EngravingSummaryResponse engraving = new EngravingSummaryResponse(
                1L,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "후반에는 과감한 공격을 선택했습니다.",
                after,
                LocalDateTime.of(2026, 8, 7, 10, 30)
        );

        EngravingListResponse response = new EngravingListResponse(List.of(engraving));

        assertThat(response.records()).containsExactly(engraving);
        assertThat(response.records().get(0).constellationData()).isEqualTo(after);
        assertThat(response.records().get(0).constellationData().points()).hasSize(2);
        assertThat(response.records().get(0).constellationData().connections())
                .containsExactly(List.of(0, 1));
    }

    @Test
    @DisplayName("각인 상세는 원본 Before와 최종 After 데이터를 함께 포함한다")
    void createsEngravingDetailResponseWithBeforeAndAfterData() {
        ConstellationShapeResponse before = createBeforeShape();
        ConstellationShapeResponse after = createAfterShape();
        ConstellationDataResponse constellationData = new ConstellationDataResponse(before, after);

        EngravingDetailResponse response = new EngravingDetailResponse(
                1L,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "후반에는 과감한 공격을 선택했습니다.",
                constellationData,
                LocalDateTime.of(2026, 8, 7, 10, 30)
        );

        assertThat(response.constellationData().before()).isEqualTo(before);
        assertThat(response.constellationData().after()).isEqualTo(after);
        assertThat(response.constellationData().before().points().get(0))
                .isEqualTo(new ConstellationPointResponse(0, 1, 3));
        assertThat(response.constellationData().after().points().get(0))
                .isEqualTo(new ConstellationPointResponse(0, 40, 300));
    }

    @Test
    @DisplayName("connections는 점 ID 쌍의 중첩 배열로 직렬화한다")
    void serializesConnectionsAsIdPairs() throws Exception {
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(createAfterShape()));

        assertThat(json.get("points").get(0).get("id").asInt()).isZero();
        assertThat(json.get("points").get(0).get("x").asInt()).isEqualTo(40);
        assertThat(json.get("connections").get(0).get(0).asInt()).isZero();
        assertThat(json.get("connections").get(0).get(1).asInt()).isEqualTo(1);
    }

    @Test
    @DisplayName("각인과 좌표 목록은 DTO 생성 후 외부에서 변경할 수 없다")
    void protectsResponseListsFromExternalChanges() {
        List<ConstellationPointResponse> points = new ArrayList<>();
        points.add(new ConstellationPointResponse(0, 40, 300));
        List<Integer> connection = new ArrayList<>(List.of(0, 1));
        List<List<Integer>> connections = new ArrayList<>();
        connections.add(connection);

        ConstellationShapeResponse shape = new ConstellationShapeResponse(points, connections);
        points.add(new ConstellationPointResponse(1, 75, 280));
        connection.set(0, 9);

        assertThat(shape.points()).hasSize(1);
        assertThat(shape.connections()).containsExactly(List.of(0, 1));
        assertThatThrownBy(() -> shape.connections().get(0).add(2))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("각인이 없으면 records 빈 배열을 유지한다")
    void createsEmptyEngravingListResponse() {
        EngravingListResponse response = new EngravingListResponse(List.of());

        assertThat(response.records()).isNotNull().isEmpty();
    }

    /**
     * 용도: Before 테스트 데이터 생성.
     * 게임판 격자 좌표와 이동 순서 연결 관계를 갖는 원본 이동 기록을 만든다.
     */
    private ConstellationShapeResponse createBeforeShape() {
        return new ConstellationShapeResponse(
                List.of(
                        new ConstellationPointResponse(0, 1, 3),
                        new ConstellationPointResponse(1, 3, 4)
                ),
                List.of(List.of(0, 1))
        );
    }

    /**
     * 용도: After 테스트 데이터 생성.
     * 300 × 300 기준 좌표와 최종 별자리 연결 관계를 갖는 재구성 데이터를 만든다.
     */
    private ConstellationShapeResponse createAfterShape() {
        return new ConstellationShapeResponse(
                List.of(
                        new ConstellationPointResponse(0, 40, 300),
                        new ConstellationPointResponse(1, 75, 280)
                ),
                List.of(List.of(0, 1))
        );
    }
}
