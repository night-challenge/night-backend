package com.nightchallenge.backend.mypage.dto.response;

import com.nightchallenge.backend.engraving.dto.response.ConstellationPointResponse;
import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;
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
 * 용도: 마이페이지 응답 DTO 구조 테스트.
 * 메인 정보와 카드 목록이 명세의 필드 구조 및 최종 After 좌표 형식으로 생성되는지 검증한다.
 */
class MyPageResponseDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("마이페이지 메인 응답은 사용자 정보와 최근 카드 정보를 포함한다")
    void createsMyPageMainResponseWithRecentCard() throws Exception {
        MyPageMainResponse response = new MyPageMainResponse(
                "민주",
                "minju",
                true,
                new RecentCardResponse(2L, "설렘의 흔적")
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("nickname").asString()).isEqualTo("민주");
        assertThat(json.get("userIdDisplay").asString()).isEqualTo("minju");
        assertThat(json.get("hasEngravingRequest").asBoolean()).isTrue();
        assertThat(json.get("recentCard").get("id").asLong()).isEqualTo(2L);
        assertThat(json.get("recentCard").get("constellationName").asString()).isEqualTo("설렘의 흔적");
    }

    @Test
    @DisplayName("최근 카드가 없으면 recentCard는 null을 유지한다")
    void createsMyPageMainResponseWithoutRecentCard() throws Exception {
        MyPageMainResponse response = new MyPageMainResponse("민주", "minju", false, null);

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("hasEngravingRequest").asBoolean()).isFalse();
        assertThat(json.get("recentCard").isNull()).isTrue();
    }

    @Test
    @DisplayName("카드 목록 항목은 최종 After 좌표 데이터만 포함한다")
    void createsEngravingCardListResponseWithAfterData() throws Exception {
        ConstellationShapeResponse after = createAfterShape();
        EngravingCardSummaryResponse card = new EngravingCardSummaryResponse(
                2L,
                "설렘의 흔적",
                after,
                LocalDateTime.of(2026, 8, 8, 14, 20)
        );

        EngravingCardListResponse response = new EngravingCardListResponse(List.of(card));
        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(response.cards()).containsExactly(card);
        assertThat(json.get("cards").get(0).get("constellationData").get("points").get(0).get("x").asInt())
                .isEqualTo(60);
        assertThat(json.get("cards").get(0).get("constellationData").get("connections").get(0).get(1).asInt())
                .isEqualTo(1);
        assertThat(json.get("cards").get(0).get("constellationData").has("before")).isFalse();
        assertThat(json.get("cards").get(0).get("constellationData").has("after")).isFalse();
    }

    @Test
    @DisplayName("카드가 없으면 cards 빈 배열을 유지한다")
    void createsEmptyEngravingCardListResponse() throws Exception {
        EngravingCardListResponse response = new EngravingCardListResponse(List.of());

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(response.cards()).isNotNull().isEmpty();
        assertThat(json.get("cards").isArray()).isTrue();
        assertThat(json.get("cards")).isEmpty();
    }

    @Test
    @DisplayName("카드 목록은 DTO 생성 후 외부에서 변경할 수 없다")
    void protectsCardListFromExternalChanges() {
        List<EngravingCardSummaryResponse> cards = new ArrayList<>();
        cards.add(new EngravingCardSummaryResponse(
                2L,
                "설렘의 흔적",
                createAfterShape(),
                LocalDateTime.of(2026, 8, 8, 14, 20)
        ));

        EngravingCardListResponse response = new EngravingCardListResponse(cards);
        cards.clear();

        assertThat(response.cards()).hasSize(1);
        assertThatThrownBy(() -> response.cards().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * 용도: After 테스트 좌표 생성.
     * 카드 목록에서 재사용하는 300 × 300 기준 최종 별자리 좌표와 연결 관계를 만든다.
     */
    private ConstellationShapeResponse createAfterShape() {
        return new ConstellationShapeResponse(
                List.of(
                        new ConstellationPointResponse(0, 60, 300),
                        new ConstellationPointResponse(1, 90, 260)
                ),
                List.of(List.of(0, 1))
        );
    }
}
