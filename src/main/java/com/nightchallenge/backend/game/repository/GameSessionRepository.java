package com.nightchallenge.backend.game.repository;

import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 용도: 게임 세션 데이터 접근.
 * 게임 세션 저장과 진행 상태·통계 조회에 필요한 데이터 접근 기능을 제공한다.
 */
public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    /**
     * 용도: 최근 진행 중 게임 조회.
     * 같은 사용자의 진행 중 게임이 여러 건이면 수정일시와 식별자 기준으로 가장 최근 한 건을 조회한다.
     */
    Optional<GameSession> findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(Long userId, GameStatus status);

    /**
     * 용도: 완료된 게임 총 플레이 횟수 조회.
     * 진행 중 상태를 제외한 승리·패배 게임의 개수를 합산한다.
     */
    long countByUserIdAndStatusNot(Long userId, GameStatus status);

    /**
     * 용도: 완료된 게임 최고 점수 조회.
     * 완료된 게임 중 점수가 가장 높은 게임 세션을 조회해 최고 점수 통계에 사용한다.
     */
    Optional<GameSession> findTopByUserIdAndStatusNotOrderByScoreDesc(Long userId, GameStatus status);
}
