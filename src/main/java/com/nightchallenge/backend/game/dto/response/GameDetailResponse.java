package com.nightchallenge.backend.game.dto.response;

import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;

import java.util.List;

/**
 * 용도: 게임 세션 상세 응답.
 * 화면 4(나이트 동선 보여주기)처럼 각인 생성 이전 시점에 나이트 이동 궤적을 미리 확인할 때 사용한다.
 * GameStateResponse와 달리 knightMoveLog를 포함한다.
 */
public record GameDetailResponse(
        Long id,
        GameMode mode,
        String fen,
        int currentTurn,
        int score,
        int targetScore,
        GameStatus status,
        List<KnightMoveLogResponse> knightMoveLog
) {

    /**
     * 용도: 게임 상세 DTO 변환.
     * GameSession Entity를 나이트 이동 궤적까지 포함한 API 응답으로 변환한다.
     */
    public static GameDetailResponse from(GameSession session) {
        List<KnightMoveLogResponse> logs = session.getKnightMoveLog().stream()
                .map(KnightMoveLogResponse::from)
                .toList();

        return new GameDetailResponse(
                session.getId(),
                session.getMode(),
                session.getFen(),
                session.getCurrentTurn(),
                session.getScore(),
                session.getTargetScore(),
                session.getStatus(),
                logs
        );
    }
}