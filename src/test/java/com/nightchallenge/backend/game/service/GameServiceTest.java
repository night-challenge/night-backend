package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.Board;
import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import com.nightchallenge.backend.game.repository.GameSessionRepository;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    @DisplayName("가장 최근에 갱신된 진행 중 게임 한 건을 반환한다")
    void getLatestActiveGame() {
        GameSession latest = new GameSession(1L, GameMode.EASY, new Board().getFen());
        given(gameSessionRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(1L, GameStatus.IN_PROGRESS))
                .willReturn(Optional.of(latest));

        assertThat(gameService.getActiveGame()).isSameAs(latest);
        verify(gameSessionRepository)
                .findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(1L, GameStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("진행 중인 게임이 없으면 GAME_NOT_FOUND 예외를 발생시킨다")
    void activeGameNotFound() {
        given(gameSessionRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(1L, GameStatus.IN_PROGRESS))
                .willReturn(Optional.empty());

        assertThatThrownBy(gameService::getActiveGame)
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GAME_NOT_FOUND));
    }
}
