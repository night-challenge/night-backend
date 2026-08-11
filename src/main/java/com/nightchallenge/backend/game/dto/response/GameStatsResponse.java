package com.nightchallenge.backend.game.dto.response;

import com.nightchallenge.backend.game.service.GameStats;

/**
 * 용도: 게임 누적 통계 응답.
 * 화면 2.1(재진입)의 최고 포인트, 플레이 횟수 표시에 사용한다.
 */
public record GameStatsResponse(
        int bestScore,
        long playCount
) {

    public static GameStatsResponse from(GameStats stats) {
        return new GameStatsResponse(stats.bestScore(), stats.playCount());
    }
}