package com.nightchallenge.backend.game.domain;

/**
 * 용도: 게임 난이도 구분.
 * 난이도별로 달라지는 목표 점수를 함께 관리해 GameSession 생성 시 목표 점수를 자동으로 결정한다.
 */
public enum GameMode {

    EASY(150),
    HARD(300);

    private final int targetScore;

    GameMode(int targetScore) {
        this.targetScore = targetScore;
    }

    /**
     * 용도: 난이도별 목표 점수 조회.
     * 15턴 안에 달성해야 하는 목표 점수를 반환한다.
     */
    public int getTargetScore() {
        return targetScore;
    }
}