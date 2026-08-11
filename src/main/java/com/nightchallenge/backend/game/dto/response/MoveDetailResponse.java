package com.nightchallenge.backend.game.dto.response;

import com.github.bhlangonijr.chesslib.PieceType;
import com.nightchallenge.backend.game.service.GameMoveService;

/**
 * 용도: 이동 한 번의 결과 응답.
 * 출발/도착 칸, 잡은 기물 종류, 획득 점수를 화면에 표시할 형태로 전달한다.
 */
public record MoveDetailResponse(
        String from,
        String to,
        String captured,
        int pointGained
) {

    /**
     * 용도: 이동 결과 DTO 변환.
     * 이동 처리 결과를 API 명세의 camelCase 응답으로 변환하며, 캡처가 없으면 captured를 null로 반환한다.
     */
    public static MoveDetailResponse from(GameMoveService.MoveOutcome outcome) {
        String captured = outcome.capturedPieceType() == PieceType.NONE
                ? null
                : outcome.capturedPieceType().name();

        return new MoveDetailResponse(
                outcome.move().getFrom().name(),
                outcome.move().getTo().name(),
                captured,
                outcome.gainedScore()
        );
    }
}