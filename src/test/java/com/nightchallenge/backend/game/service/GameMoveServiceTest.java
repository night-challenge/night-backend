package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.Board;
import com.nightchallenge.backend.game.domain.GameStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GameMoveServiceTest {

    private final GameMoveService gameMoveService = new GameMoveService(
            mock(ScoreTable.class),
            mock(AiOpponentService.class)
    );

    @Test
    @DisplayName("15번째 사용자 이동에서 목표 점수를 달성하면 승리한다")
    void winsWhenTargetReachedOnFifteenthTurn() {
        GameStatus status = ReflectionTestUtils.invokeMethod(
                gameMoveService,
                "determineStatus",
                150,
                150,
                15,
                new Board()
        );

        assertThat(status).isEqualTo(GameStatus.WON);
    }

    @Test
    @DisplayName("15번째 사용자 이동에도 목표 점수에 미달하면 패배한다")
    void losesWhenTargetNotReachedOnFifteenthTurn() {
        GameStatus status = ReflectionTestUtils.invokeMethod(
                gameMoveService,
                "determineStatus",
                150,
                140,
                15,
                new Board()
        );

        assertThat(status).isEqualTo(GameStatus.LOST);
    }
}
