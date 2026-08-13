package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.PieceType;
import com.nightchallenge.backend.game.domain.GameMode;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 용도: 난이도별 기물 캡처 점수 제공.
 * 나이트로 잡은 상대 기물의 종류와 현재 게임 난이도에 따라 획득 점수를 결정한다.
 */
@Component
public class ScoreTable {

    private static final Map<GameMode, Map<PieceType, Integer>> TABLE = Map.of(
            GameMode.EASY, Map.of(
                    PieceType.PAWN, 20,
                    PieceType.KNIGHT, 30,
                    PieceType.BISHOP, 40,
                    PieceType.ROOK, 50,
                    PieceType.QUEEN, 70
            ),
            GameMode.HARD, Map.of(
                    PieceType.PAWN, 15,
                    PieceType.KNIGHT, 25,
                    PieceType.BISHOP, 35,
                    PieceType.ROOK, 45,
                    PieceType.QUEEN, 60
            )
    );

    /**
     * 용도: 캡처 점수 조회.
     * 난이도와 잡은 기물 종류에 해당하는 점수를 반환한다. 킹처럼 표에 없는 기물은 0점을 반환한다.
     */
    public int getPoint(GameMode mode, PieceType capturedType) {
        return TABLE.get(mode).getOrDefault(capturedType, 0);
    }
}