package com.nightchallenge.backend.game.service;

/**
 * 용도: 게임 누적 통계 표현.
 * 화면 2.1(재진입)에서 표시할 최고 점수와 총 플레이 횟수를 담는다.
 * 진행 중(IN_PROGRESS)인 게임은 아직 결과가 확정되지 않았으므로 집계에서 제외한다.
 */
public record GameStats(
        int bestScore,
        long playCount
) {
}