package com.nightchallenge.backend.engravingrequest.dto;

import com.nightchallenge.backend.engraving.dto.response.ConstellationPointResponse;
import com.nightchallenge.backend.engraving.dto.response.ConstellationShapeResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingSummaryResponse;
import com.nightchallenge.backend.engravingrequest.domain.EngravingColor;
import com.nightchallenge.backend.engravingrequest.dto.request.EngravingRequestCreateRequest;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestCreateResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestListResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestProductResponse;
import com.nightchallenge.backend.engravingrequest.dto.response.EngravingRequestSummaryResponse;
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
 * 용도: 각인 신청 DTO 구조 테스트.
 * 신청 생성과 목록 응답이 명세의 필드 구조를 유지하고 응답 목록을 안전하게 보관하는지 검증한다.
 */
class EngravingRequestDtoTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("신청 생성 요청은 각인 ID와 제품 옵션 ID 및 색상을 전달한다")
    void createsEngravingRequestCreateRequest() {
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(
                1L,
                3L,
                EngravingColor.GOLD
        );

        assertThat(request.nightPathRecordId()).isEqualTo(1L);
        assertThat(request.productOptionId()).isEqualTo(3L);
        assertThat(request.engravingColor()).isEqualTo(EngravingColor.GOLD);
    }

    @Test
    @DisplayName("신청 생성 요청은 조합 검증을 위해 각인과 색상의 누락 상태를 보존한다")
    void preservesMissingSelectionsForServiceValidation() {
        EngravingRequestCreateRequest request = new EngravingRequestCreateRequest(null, 3L, null);

        assertThat(request.nightPathRecordId()).isNull();
        assertThat(request.engravingColor()).isNull();
    }

    @Test
    @DisplayName("신청 생성 응답은 id와 productCode를 camelCase로 직렬화한다")
    void serializesEngravingRequestCreateResponse() throws Exception {
        EngravingRequestCreateResponse response = new EngravingRequestCreateResponse(5L, "NWdfw25");

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("id").asLong()).isEqualTo(5L);
        assertThat(json.get("productCode").asString()).isEqualTo("NWdfw25");
    }

    @Test
    @DisplayName("신청 목록은 제품 정보와 기존 각인 요약 응답을 함께 포함한다")
    void createsEngravingRequestListResponse() {
        EngravingRequestProductResponse product = new EngravingRequestProductResponse(
                "L 비세토스 수트케이스",
                "갈색"
        );
        EngravingSummaryResponse engraving = createEngravingSummary();
        EngravingRequestSummaryResponse record = new EngravingRequestSummaryResponse(
                5L,
                "NWdfw25",
                EngravingColor.GOLD,
                product,
                engraving
        );

        EngravingRequestListResponse response = new EngravingRequestListResponse(List.of(record));

        assertThat(response.records()).containsExactly(record);
        assertThat(response.records().get(0).product()).isEqualTo(product);
        assertThat(response.records().get(0).engraving()).isEqualTo(engraving);
        assertThat(response.records().get(0).engraving().constellationData())
                .isEqualTo(engraving.constellationData());
    }

    @Test
    @DisplayName("신청 목록이 없으면 records는 빈 배열을 유지한다")
    void createsEmptyEngravingRequestListResponse() {
        EngravingRequestListResponse response = new EngravingRequestListResponse(List.of());

        assertThat(response.records()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("신청 목록은 DTO 생성 후 외부에서 변경할 수 없다")
    void protectsEngravingRequestListFromExternalChanges() {
        List<EngravingRequestSummaryResponse> records = new ArrayList<>();
        EngravingRequestSummaryResponse record = new EngravingRequestSummaryResponse(
                5L,
                "NWdfw25",
                EngravingColor.GOLD,
                new EngravingRequestProductResponse("L 비세토스 수트케이스", "갈색"),
                createEngravingSummary()
        );
        records.add(record);

        EngravingRequestListResponse response = new EngravingRequestListResponse(records);
        records.clear();

        assertThat(response.records()).containsExactly(record);
        assertThatThrownBy(() -> response.records().add(record))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * 용도: 신청 목록용 각인 테스트 데이터 생성.
     * 기존 EngravingSummaryResponse가 최종 After 좌표를 신청 목록에서도 재사용하는 구조를 만든다.
     */
    private EngravingSummaryResponse createEngravingSummary() {
        ConstellationShapeResponse after = new ConstellationShapeResponse(
                List.of(
                        new ConstellationPointResponse(0, 40, 300),
                        new ConstellationPointResponse(1, 75, 280)
                ),
                List.of(List.of(0, 1))
        );

        return new EngravingSummaryResponse(
                1L,
                "오리온의 흔적",
                List.of("침착함", "역전", "도전"),
                "초반에는 신중하게 전개했지만, 후반에는 과감한 공격을 선택했습니다.",
                after,
                LocalDateTime.of(2026, 8, 7, 10, 30)
        );
    }
}
