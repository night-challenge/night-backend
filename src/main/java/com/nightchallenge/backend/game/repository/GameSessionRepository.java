package com.nightchallenge.backend.game.repository;

import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 용도: 게임 세션 데이터 접근.
 * GameSession의 저장과 조회를 제공하고, 사용자가 이어할 수 있는 진행 중인 게임과 누적 통계를 조회한다.
 */
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    /**
     * 용도: 진행 중인 게임 조회.
     * 화면 2.1(재진입)에서 이어할 게임이 있는지 확인할 때 사용한다.
     * 한 사용자가 동시에 하나의 게임만 진행한다고 가정한다.
     */
    Optional<GameSession> findByUserIdAndStatus(Long userId, GameStatus status);

    /**
     * 용도: 완료된 게임 총 플레이 횟수 조회.
     * 진행 중(IN_PROGRESS)인 게임은 아직 결과가 확정되지 않았으므로 집계에서 제외한다.
     */
    long countByUserIdAndStatusNot(Long userId, GameStatus status);

    /**
     * 용도: 최고 점수 게임 조회.
     * 완료된 게임 중 점수가 가장 높은 게임 세션을 조회해 최고 점수 통계에 사용한다.
     */
    Optional<GameSession> findTopByUserIdAndStatusNotOrderByScoreDesc(Long userId, GameStatus status);
}