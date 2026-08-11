package com.nightchallenge.backend.game.dto.response;

import com.nightchallenge.backend.game.service.GameMoveService;

/**
 * 용도: 한 턴 처리 결과 응답.
 * 사용자 이동, AI 응수(게임이 종료됐다면 null), 갱신된 게임 상태를 함께 전달한다.
 */
public record MoveResultResponse(
        MoveDetailResponse userMove,
        MoveDetailResponse aiMove,
        GameStateResponse gameState
) {

    /**
     * 용도: 턴 처리 결과 DTO 변환.
     * 이동 처리 서비스의 결과를 API 명세의 camelCase 응답으로 변환한다.
     */
    public static MoveResultResponse from(GameMoveService.MoveResult result) {
        MoveDetailResponse aiMove = result.aiOutcome() == null
                ? null
                : MoveDetailResponse.from(result.aiOutcome());

        return new MoveResultResponse(
                MoveDetailResponse.from(result.userOutcome()),
                aiMove,
                GameStateResponse.from(result.session())
        );
    }
}