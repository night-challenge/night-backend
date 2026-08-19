package com.nightchallenge.backend.mypage.service;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.repository.NightPathRecordRepository;
import com.nightchallenge.backend.engravingrequest.domain.EngravingRequestStatus;
import com.nightchallenge.backend.engravingrequest.repository.EngravingRequestRepository;
import com.nightchallenge.backend.mypage.dto.response.EngravingCardListResponse;
import com.nightchallenge.backend.mypage.dto.response.MyPageMainResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 용도: 마이페이지 Service 단위 검증.
 * 신청 여부와 최근 카드 조회 및 최종 After만 포함한 카드 목록 변환을 Repository 대역으로 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @Mock
    private NightPathRecordRepository nightPathRecordRepository;

    @Mock
    private EngravingRequestRepository engravingRequestRepository;

    private MyPageService myPageService;

    @BeforeEach
    void setUp() {
        myPageService = new MyPageService(nightPathRecordRepository, engravingRequestRepository);
    }

    @Test
    @DisplayName("마이페이지 메인에 사용자 정보와 신청 여부 및 최근 카드를 반환한다")
    void getMyPageMain() {
        NightPathRecord recentCard = createNightPathRecord(
                2L,
                "설렘의 흔적",
                LocalDateTime.of(2026, 8, 8, 14, 20)
        );
        when(engravingRequestRepository.existsByUserIdAndStatus(1L, EngravingRequestStatus.COMPLETED))
                .thenReturn(true);
        when(nightPathRecordRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(recentCard));

        MyPageMainResponse response = myPageService.getMyPageMain();

        assertThat(response.nickname()).isEqualTo("사자후");
        assertThat(response.userIdDisplay()).isEqualTo("sajahoo");
        assertThat(response.hasEngravingRequest()).isTrue();
        assertThat(response.recentCard().id()).isEqualTo(2L);
        assertThat(response.recentCard().constellationName()).isEqualTo("설렘의 흔적");
    }

    @Test
    @DisplayName("보유 카드가 없으면 최근 카드를 null로 반환한다")
    void getMyPageMainWithoutRecentCard() {
        when(engravingRequestRepository.existsByUserIdAndStatus(1L, EngravingRequestStatus.COMPLETED))
                .thenReturn(false);
        when(nightPathRecordRepository.findFirstByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());

        MyPageMainResponse response = myPageService.getMyPageMain();

        assertThat(response.hasEngravingRequest()).isFalse();
        assertThat(response.recentCard()).isNull();
    }

    @Test
    @DisplayName("카드 모음은 최신순 각인과 최종 After 좌표만 반환한다")
    void getEngravingCards() {
        NightPathRecord latest = createNightPathRecord(
                2L,
                "설렘의 흔적",
                LocalDateTime.of(2026, 8, 8, 14, 20)
        );
        NightPathRecord older = createNightPathRecord(
                1L,
                "오리온의 흔적",
                LocalDateTime.of(2026, 8, 7, 10, 30)
        );
        when(nightPathRecordRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(latest, older));

        EngravingCardListResponse response = myPageService.getEngravingCards();

        assertThat(response.cards()).extracting(card -> card.id()).containsExactly(2L, 1L);
        assertThat(response.cards().get(0).constellationData().points())
                .extracting(point -> point.id())
                .containsExactly(102, 103);
        assertThat(response.cards().get(0).constellationData().connections())
                .containsExactly(List.of(102, 103));
        verify(nightPathRecordRepository).findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("보유 카드가 없으면 빈 cards 배열을 반환한다")
    void getEmptyEngravingCards() {
        when(nightPathRecordRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        EngravingCardListResponse response = myPageService.getEngravingCards();

        assertThat(response.cards()).isEmpty();
    }

    /**
     * 용도: 테스트용 보유 각인 생성.
     * Before와 After의 점 ID를 다르게 구성해 카드 응답에 After만 포함되는지 확인한다.
     */
    private NightPathRecord createNightPathRecord(Long id, String name, LocalDateTime createdAt) {
        int pointId = id.intValue();
        ConstellationShape before = new ConstellationShape(
                List.of(new ConstellationPoint(pointId, 1, 3)),
                List.of()
        );
        ConstellationShape after = new ConstellationShape(
                List.of(
                        new ConstellationPoint(100 + pointId, 40, 250),
                        new ConstellationPoint(101 + pointId, 120, 180)
                ),
                List.of(List.of(100 + pointId, 101 + pointId))
        );
        NightPathRecord engraving = new NightPathRecord(
                1L,
                100L + id,
                name,
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                new ConstellationData(before, after)
        );
        ReflectionTestUtils.setField(engraving, "id", id);
        ReflectionTestUtils.setField(engraving, "createdAt", createdAt);
        return engraving;
    }
}
