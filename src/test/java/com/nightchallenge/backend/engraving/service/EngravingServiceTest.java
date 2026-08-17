package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationPoint;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.dto.response.EngravingDetailResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingListResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingNameUpdateResponse;
import com.nightchallenge.backend.engraving.repository.NightPathRecordRepository;
import com.nightchallenge.backend.game.service.GameService;
import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;

/**
 * 용도: 보유 각인 Service 단위 테스트.
 * Repository를 대체한 Mock으로 목록·상세 DTO 변환과 이름 수정 및 예외 처리 규칙을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class EngravingServiceTest {

    @Mock
    private GameService gameService;

    @Mock
    private ConstellationGenerationService constellationGenerationService;

    @Mock
    private PlayAnalyzer playAnalyzer;

    @Mock
    private NightPathRecordRepository nightPathRecordRepository;

    @InjectMocks
    private EngravingService engravingService;

    @Test
    @DisplayName("동일 게임의 기존 각인이 있으면 추가 저장 없이 기존 기록을 반환한다")
    void createFromGameSessionReturnsExistingRecord() {
        NightPathRecord existing = createRecord(1L, "기존 각인");
        given(nightPathRecordRepository.findByGameSessionId(101L))
                .willReturn(Optional.of(existing));

        EngravingService.CreationResult result = engravingService.createFromGameSession(101L);

        assertThat(result.created()).isFalse();
        assertThat(result.record()).isSameAs(existing);
        verify(nightPathRecordRepository, never()).save(any(NightPathRecord.class));
        verify(gameService, never()).getGameSession(any(Long.class));
    }

    @Test
    @DisplayName("각인이 없는 승리 게임은 한 번 저장하고 신규 생성 결과를 반환한다")
    void createFromGameSessionSavesNewRecordOnce() {
        GameSession session = new GameSession(1L, GameMode.EASY, new com.github.bhlangonijr.chesslib.Board().getFen());
        ReflectionTestUtils.setField(session, "status", GameStatus.WON);
        ConstellationData data = createRecord(9L, "형태").getConstellationData();

        given(nightPathRecordRepository.findByGameSessionId(10L)).willReturn(Optional.empty());
        given(gameService.getGameSession(10L)).willReturn(session);
        given(constellationGenerationService.generate(session.getKnightMoveLog())).willReturn(data);
        given(playAnalyzer.analyze(session))
                .willReturn(new PlayAnalysisResult("새 각인", List.of("도전"), "분석"));
        given(nightPathRecordRepository.save(any(NightPathRecord.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        EngravingService.CreationResult result = engravingService.createFromGameSession(10L);

        assertThat(result.created()).isTrue();
        assertThat(result.record().getGameSessionId()).isEqualTo(10L);
        assertThat(result.record().getConstellationName()).isEqualTo("새 각인");
        verify(nightPathRecordRepository).save(any(NightPathRecord.class));
    }

    @Test
    @DisplayName("승리하지 않은 게임의 각인 생성은 400 예외를 유지한다")
    void createFromGameSessionRejectsUnfinishedGame() {
        GameSession session = new GameSession(1L, GameMode.EASY, new com.github.bhlangonijr.chesslib.Board().getFen());
        given(nightPathRecordRepository.findByGameSessionId(10L)).willReturn(Optional.empty());
        given(gameService.getGameSession(10L)).willReturn(session);

        assertThatThrownBy(() -> engravingService.createFromGameSession(10L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST));
        verify(nightPathRecordRepository, never()).save(any(NightPathRecord.class));
    }

    @Test
    @DisplayName("존재하지 않는 게임의 각인 생성은 404 예외를 유지한다")
    void createFromGameSessionKeepsGameNotFound() {
        given(nightPathRecordRepository.findByGameSessionId(999L)).willReturn(Optional.empty());
        given(gameService.getGameSession(999L))
                .willThrow(new BusinessException(ErrorCode.GAME_NOT_FOUND));

        assertThatThrownBy(() -> engravingService.createFromGameSession(999L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GAME_NOT_FOUND));
    }

    @Test
    @DisplayName("재생성하면 before와 분석 정보는 유지하고 after만 변경한다")
    void regenerateChangesOnlyAfter() {
        NightPathRecord record = createRecord(1L, "기존 각인");
        ConstellationShape originalBefore = record.getConstellationData().before();
        ConstellationShape newAfter = new ConstellationShape(
                List.of(new ConstellationPoint(20, 150, 150)),
                List.of()
        );
        List<String> originalKeywords = record.getKeywords();
        String originalComment = record.getComment();
        given(nightPathRecordRepository.findById(1L)).willReturn(Optional.of(record));
        given(constellationGenerationService.regenerateAfter(originalBefore)).willReturn(newAfter);

        NightPathRecord result = engravingService.regenerate(1L);

        assertThat(result.getConstellationData().before()).isSameAs(originalBefore);
        assertThat(result.getConstellationData().after()).isSameAs(newAfter);
        assertThat(result.getConstellationName()).isEqualTo("기존 각인");
        assertThat(result.getKeywords()).isSameAs(originalKeywords);
        assertThat(result.getComment()).isEqualTo(originalComment);
    }

    @Test
    @DisplayName("고정 사용자의 각인 목록을 최신순 Repository 결과대로 변환한다")
    void getEngravings() {
        NightPathRecord latest = createRecord(2L, "최신 별자리");
        NightPathRecord older = createRecord(1L, "이전 별자리");
        given(nightPathRecordRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(latest, older));

        EngravingListResponse response = engravingService.getEngravings();

        assertThat(response.records()).extracting(record -> record.id())
                .containsExactly(2L, 1L);
        assertThat(response.records().get(0).constellationData().points())
                .extracting(point -> point.id())
                .containsExactly(10, 11);
        assertThat(response.records().get(0).constellationData().connections())
                .containsExactly(List.of(10, 11));
        verify(nightPathRecordRepository).findAllByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("보유 각인이 없으면 records 빈 배열을 반환한다")
    void getEngravingsReturnsEmptyRecords() {
        given(nightPathRecordRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of());

        EngravingListResponse response = engravingService.getEngravings();

        assertThat(response.records()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("각인 상세 응답에 before와 after를 모두 포함한다")
    void getEngraving() {
        NightPathRecord record = createRecord(1L, "오리온의 흔적");
        given(nightPathRecordRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(record));

        EngravingDetailResponse response = engravingService.getEngraving(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.constellationData().before().points())
                .extracting(point -> point.id())
                .containsExactly(0, 1);
        assertThat(response.constellationData().after().points())
                .extracting(point -> point.id())
                .containsExactly(10, 11);
    }

    @Test
    @DisplayName("존재하지 않는 각인 상세 조회는 404 비즈니스 예외를 발생시킨다")
    void getEngravingNotFound() {
        given(nightPathRecordRepository.findByIdAndUserId(999L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> engravingService.getEngraving(999L))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENGRAVING_NOT_FOUND));
    }

    @Test
    @DisplayName("각인 이름을 수정하고 변경된 이름을 응답한다")
    void updateEngravingName() {
        NightPathRecord record = createRecord(1L, "기존 이름");
        given(nightPathRecordRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(record));

        EngravingNameUpdateResponse response = engravingService.updateEngravingName(1L, "설렘의 흔적");

        assertThat(record.getConstellationName()).isEqualTo("설렘의 흔적");
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.constellationName()).isEqualTo("설렘의 흔적");
    }

    @Test
    @DisplayName("기존 이름과 동일하면 400 비즈니스 예외를 발생시킨다")
    void updateEngravingNameUnchanged() {
        NightPathRecord record = createRecord(1L, "오리온의 흔적");
        given(nightPathRecordRepository.findByIdAndUserId(1L, 1L))
                .willReturn(Optional.of(record));

        assertThatThrownBy(() -> engravingService.updateEngravingName(1L, "오리온의 흔적"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENGRAVING_NAME_UNCHANGED));
    }

    @Test
    @DisplayName("존재하지 않는 각인 이름 수정은 404 비즈니스 예외를 발생시킨다")
    void updateEngravingNameNotFound() {
        given(nightPathRecordRepository.findByIdAndUserId(999L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> engravingService.updateEngravingName(999L, "새 이름"))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENGRAVING_NOT_FOUND));
    }

    /**
     * 용도: 테스트용 각인 생성.
     * before와 after가 구분된 각인 Entity를 만들고 테스트에서 사용할 식별자를 설정한다.
     */
    private NightPathRecord createRecord(Long id, String constellationName) {
        ConstellationShape before = new ConstellationShape(
                List.of(
                        new ConstellationPoint(0, 1, 3),
                        new ConstellationPoint(1, 3, 4)
                ),
                List.of(List.of(0, 1))
        );
        ConstellationShape after = new ConstellationShape(
                List.of(
                        new ConstellationPoint(10, 40, 250),
                        new ConstellationPoint(11, 120, 180)
                ),
                List.of(List.of(10, 11))
        );
        NightPathRecord record = new NightPathRecord(
                1L,
                100L + id,
                constellationName,
                List.of("침착함", "역전", "도전"),
                "플레이 분석 코멘트",
                new ConstellationData(before, after)
        );
        ReflectionTestUtils.setField(record, "id", id);
        return record;
    }
}
