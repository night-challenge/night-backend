package com.nightchallenge.backend.game.repository;

import com.github.bhlangonijr.chesslib.Board;
import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GameSessionRepositoryTest {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Test
    @DisplayName("진행 중 게임이 없으면 빈 결과를 반환한다")
    void findActiveGameReturnsEmpty() {
        Optional<GameSession> result = gameSessionRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(1L, GameStatus.IN_PROGRESS);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("진행 중 게임이 한 건이면 해당 게임을 반환한다")
    void findSingleActiveGame() {
        GameSession saved = gameSessionRepository.save(newSession());

        Optional<GameSession> result = gameSessionRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(1L, GameStatus.IN_PROGRESS);

        assertThat(result).contains(saved);
    }

    @Test
    @DisplayName("진행 중 게임이 여러 건이면 가장 최근에 갱신된 게임을 반환한다")
    void findLatestAmongMultipleActiveGames() {
        GameSession older = newSession();
        ReflectionTestUtils.setField(older, "updatedAt", LocalDateTime.of(2026, 8, 16, 10, 0));
        gameSessionRepository.save(older);

        GameSession latest = newSession();
        ReflectionTestUtils.setField(latest, "updatedAt", LocalDateTime.of(2026, 8, 16, 11, 0));
        gameSessionRepository.save(latest);

        Optional<GameSession> result = gameSessionRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(1L, GameStatus.IN_PROGRESS);

        assertThat(result).contains(latest);
    }

    private GameSession newSession() {
        return new GameSession(1L, GameMode.EASY, new Board().getFen());
    }
}
