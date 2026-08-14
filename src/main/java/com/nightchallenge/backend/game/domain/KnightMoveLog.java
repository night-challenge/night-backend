package com.nightchallenge.backend.game.domain;

/**
 * 용도: 나이트 이동 기록 표현.
 * 나이트가 이동할 때마다 턴과 출발/도착 칸 좌표를 기록해, 승리 시 각인의 원본 궤적(before)으로 사용한다.
 */
public record KnightMoveLog(
        int turn,
        int fromX,
        int fromY,
        int toX,
        int toY
) {
}