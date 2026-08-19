package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GameMoveServiceTest {

    private final ScoreTable scoreTable = mock(ScoreTable.class);
    private final AiOpponentService aiOpponentService = mock(AiOpponentService.class);
    private final GameMoveService gameMoveService = new GameMoveService(
            scoreTable,
            aiOpponentService
    );

    @Test
    @DisplayName("15번째 사용자 이동에서 목표 점수를 달성하면 승리한다")
    void winsWhenTargetReachedOnFifteenthTurn() {
        Board boardWithoutUserKnights = new Board();
        boardWithoutUserKnights.loadFromFen("k7/8/8/8/8/8/8/7K w - - 0 1");

        GameStatus status = ReflectionTestUtils.invokeMethod(
                gameMoveService,
                "determineStatus",
                150,
                150,
                15,
                boardWithoutUserKnights
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

    @Test
    @DisplayName("마지막 사용자 나이트가 AI 응수로 잡히면 패배한다")
    void losesWhenAiCapturesLastUserKnight() {
        String fen = "k7/8/8/8/1b6/8/8/1N5K w - - 0 1";
        GameSession session = new GameSession(1L, GameMode.EASY, fen);
        given(aiOpponentService.selectMove(org.mockito.ArgumentMatchers.any(Board.class)))
                .willReturn(new Move(Square.B4, Square.C3));

        GameMoveService.MoveResult result = gameMoveService.processTurn(session, "B1", "C3");

        assertThat(result.aiOutcome()).isNotNull();
        assertThat(result.session().getStatus()).isEqualTo(GameStatus.LOST);
        assertThat(result.session().getCurrentTurn()).isEqualTo(1);

        assertThatThrownBy(() -> gameMoveService.processTurn(session, "H1", "H2"))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode())
                .isEqualTo(ErrorCode.GAME_ALREADY_FINISHED);
    }

    @Test
    @DisplayName("사용자 나이트가 남아 있고 종료 조건이 없으면 게임을 계속한다")
    void continuesWhenUserKnightRemains() {
        GameStatus status = ReflectionTestUtils.invokeMethod(
                gameMoveService,
                "determineStatus",
                150,
                0,
                1,
                new Board()
        );

        assertThat(status).isEqualTo(GameStatus.IN_PROGRESS);
    }
}
