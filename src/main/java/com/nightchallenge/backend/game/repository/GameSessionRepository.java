package com.nightchallenge.backend.game.repository;

import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 용도: 게임 세션 데이터 접근.
 * GameSession의 저장과 조회를 제공하고, 사용자가 이어할 수 있는 진행 중인 게임을 조회한다.
 */
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    /**
     * 용도: 진행 중인 게임 조회.
     * 화면 2.1(재진입)에서 이어할 게임이 있는지 확인할 때 사용한다.
     * 한 사용자가 동시에 하나의 게임만 진행한다고 가정한다.
     */
    Optional<GameSession> findByUserIdAndStatus(Long userId, GameStatus status);
}