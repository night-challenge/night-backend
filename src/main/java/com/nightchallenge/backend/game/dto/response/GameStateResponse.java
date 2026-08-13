package com.nightchallenge.backend.game.dto.response;

import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;

/**
 * 용도: 게임 상태 응답.
 * 현재 턴, 점수, 목표 점수, 진행 상태 등 화면에 표시할 게임 세션의 현재 상태를 전달한다.
 */
public record GameStateResponse(
        Long id,
        GameMode mode,
        String fen,
        int currentTurn,
        int score,
        int targetScore,
        GameStatus status
) {

    /**
     * 용도: 게임 상태 DTO 변환.
     * GameSession Entity를 API 명세의 camelCase 응답으로 변환한다.
     */
    public static GameStateResponse from(GameSession session) {
        return new GameStateResponse(
                session.getId(),
                session.getMode(),
                session.getFen(),
                session.getCurrentTurn(),
                session.getScore(),
                session.getTargetScore(),
                session.getStatus()
        );
    }
}