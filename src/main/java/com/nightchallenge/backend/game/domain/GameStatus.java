package com.nightchallenge.backend.game.domain;

/**
 * 용도: 게임 진행 상태 구분.
 * 게임이 진행 중인지, 목표 점수 달성으로 승리했는지, 체크메이트나 턴 초과로 패배했는지를 나타낸다.
 */
public enum GameStatus {
    IN_PROGRESS,
    WON,
    LOST
}