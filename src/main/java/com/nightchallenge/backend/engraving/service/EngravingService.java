package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.engraving.domain.ConstellationShape;
import com.nightchallenge.backend.engraving.domain.NightPathRecord;
import com.nightchallenge.backend.engraving.dto.response.EngravingDetailResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingListResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingNameUpdateResponse;
import com.nightchallenge.backend.engraving.dto.response.EngravingSummaryResponse;
import com.nightchallenge.backend.engraving.repository.NightPathRecordRepository;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import com.nightchallenge.backend.game.service.GameService;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 용도: 각인(별자리) 생성 및 재생성.
 * 승리한 게임 세션의 나이트 이동 궤적을 별자리로 재구성하고, 플레이 분석 결과와 함께 각인을 생성해 저장한다.
 * 또한 이미 생성된 각인의 최종 별자리(after)를 원본 궤적(before)은 유지한 채 다시 생성한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EngravingService {

    private final GameService gameService;
    private final ConstellationGenerationService constellationGenerationService;
    private final PlayAnalyzer playAnalyzer;
    private final NightPathRecordRepository nightPathRecordRepository;

    /**
     * 로그인 기능 도입 전까지 사용하는 고정 사용자 식별자.
     */
    private static final Long TEMP_USER_ID = 1L;

    /**
     * 용도: 보유 각인 목록 조회.
     * 고정 사용자의 각인을 생성일시 기준 최신순으로 조회하고 최종 after 별자리만 포함한 목록 응답으로 변환한다.
     */
    @Transactional(readOnly = true)
    public EngravingListResponse getEngravings() {
        return new EngravingListResponse(
                nightPathRecordRepository.findAllByUserIdOrderByCreatedAtDesc(TEMP_USER_ID).stream()
                        .map(EngravingSummaryResponse::from)
                        .toList()
        );
    }

    /**
     * 용도: 보유 각인 상세 조회.
     * 고정 사용자가 보유한 각인을 조회하고 원본 before와 최종 after를 모두 포함한 상세 응답으로 변환한다.
     */
    @Transactional(readOnly = true)
    public EngravingDetailResponse getEngraving(Long engravingId) {
        return EngravingDetailResponse.from(findOwnedEngraving(engravingId));
    }

    /**
     * 용도: 보유 각인 이름 수정.
     * 고정 사용자의 각인 이름이 기존 값과 다른지 확인한 뒤 새 이름을 반영하고 수정 결과를 반환한다.
     */
    public EngravingNameUpdateResponse updateEngravingName(Long engravingId, String constellationName) {
        NightPathRecord record = findOwnedEngraving(engravingId);

        if (record.getConstellationName().equals(constellationName)) {
            throw new BusinessException(ErrorCode.ENGRAVING_NAME_UNCHANGED);
        }

        record.rename(constellationName);
        return EngravingNameUpdateResponse.from(record);
    }

    /**
     * 용도: 사용자 소유 각인 조회.
     * 각인 식별자와 고정 사용자 식별자가 일치하는 데이터를 찾고 없으면 404 예외를 발생시킨다.
     */
    private NightPathRecord findOwnedEngraving(Long engravingId) {
        return nightPathRecordRepository.findByIdAndUserId(engravingId, TEMP_USER_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENGRAVING_NOT_FOUND));
    }

    /**
     * 용도: 게임 승리 결과로 각인 생성.
     * 승리한 게임 세션의 나이트 이동 궤적을 별자리로 재구성하고, 플레이 분석 결과와 함께
     * 새 각인(NightPathRecord)을 생성해 저장한다. 승리한 게임이 아니면 예외를 발생시킨다.
     * 동일한 게임 세션으로 이미 각인이 생성된 적이 있으면, 새로 생성하지 않고 기존 각인을 그대로 반환한다.
     * 화면 5.1에서 [<] 버튼으로 화면 4로 돌아갔다가 다시 각인 생성 단계로 진입해 API가 재호출되는 경우를 대비한 것이다.
     */
    public NightPathRecord createFromGameSession(Long gameSessionId) {
        return nightPathRecordRepository.findByGameSessionId(gameSessionId)
                .orElseGet(() -> createNewRecord(gameSessionId));
    }

    /**
     * 용도: 새 각인 생성.
     * 게임 승리 여부를 검증한 뒤, 궤적 재구성과 플레이 분석을 거쳐 새 각인을 저장한다.
     */
    private NightPathRecord createNewRecord(Long gameSessionId) {
        GameSession session = gameService.getGameSession(gameSessionId);

        if (session.getStatus() != GameStatus.WON) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "승리한 게임에서만 각인을 생성할 수 있습니다.");
        }

        ConstellationData constellationData = constellationGenerationService.generate(session.getKnightMoveLog());
        PlayAnalysisResult analysis = playAnalyzer.analyze(session);

        NightPathRecord record = new NightPathRecord(
                TEMP_USER_ID,
                gameSessionId,
                analysis.constellationName(),
                analysis.keywords(),
                analysis.comment(),
                constellationData
        );

        return nightPathRecordRepository.save(record);
    }

    /**
     * 용도: 각인 별자리 재생성.
     * 기존 각인의 원본 이동 궤적(before)은 그대로 유지한 채, 재구성 로직을 다시 실행해
     * 최종 별자리(after)만 새로운 모양으로 교체한다. 존재하지 않는 각인이면 404 예외를 발생시킨다.
     */
    public NightPathRecord regenerate(Long engravingId) {
        NightPathRecord record = nightPathRecordRepository.findById(engravingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENGRAVING_NOT_FOUND));

        ConstellationShape currentBefore = record.getConstellationData().before();
        ConstellationShape newAfter = constellationGenerationService.regenerateAfter(currentBefore);

        record.regenerateAfter(newAfter);

        return record;
    }
}
