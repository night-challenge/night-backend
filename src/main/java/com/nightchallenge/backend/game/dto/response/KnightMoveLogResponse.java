package com.nightchallenge.backend.game.dto.response;

import com.nightchallenge.backend.game.domain.KnightMoveLog;

/**
 * 용도: 나이트 이동 기록 응답.
 * 화면 4(나이트 동선 보여주기)에서 각인 생성 전 원본 궤적을 미리 보여줄 때 사용한다.
 */
public record KnightMoveLogResponse(
        int turn,
        int fromX,
        int fromY,
        int toX,
        int toY
) {

    public static KnightMoveLogResponse from(KnightMoveLog log) {
        return new KnightMoveLogResponse(log.turn(), log.fromX(), log.fromY(), log.toX(), log.toY());
    }
}