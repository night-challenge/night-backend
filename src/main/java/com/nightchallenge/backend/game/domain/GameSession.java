package com.nightchallenge.backend.game.domain;

import com.nightchallenge.backend.game.domain.converter.KnightMoveLogListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 용도: 진행 중이거나 완료된 게임 한 판의 상태 저장.
 * 체스 보드 상태, 턴과 점수, 나이트 이동 기록을 관리해 게임 중단 후 재진입 시 이어서 진행할 수 있게 한다.
 */
@Getter
@Entity
@Table(name = "game_sessions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameSession {

    private static final int MAX_TURN = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 소유자 식별자. 로그인 기능 도입 전까지는 고정값(1)을 사용한다.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "VARCHAR(10)")
    private GameMode mode;

    /**
     * 체스 보드의 현재 상태를 FEN 표기법 문자열로 저장한다.
     */
    @Column(nullable = false, length = 100)
    private String fen;

    @Column(name = "current_turn", nullable = false)
    private int currentTurn;

    @Column(nullable = false)
    private int score;

    @Column(name = "target_score", nullable = false)
    private int targetScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private GameStatus status;

    @Convert(converter = KnightMoveLogListJsonConverter.class)
    @Column(name = "knight_move_log", nullable = false, columnDefinition = "JSON")
    private List<KnightMoveLog> knightMoveLog = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 용도: 새 게임 시작.
     * 선택한 난이도에 맞는 목표 점수와 초기 보드 상태로 게임 세션을 생성한다.
     */
    public GameSession(Long userId, GameMode mode, String initialFen) {
        this.userId = userId;
        this.mode = mode;
        this.fen = initialFen;
        this.currentTurn = 0;
        this.score = 0;
        this.targetScore = mode.getTargetScore();
        this.status = GameStatus.IN_PROGRESS;
        this.knightMoveLog = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * 용도: 나이트 이동 기록 조회.
     * 외부에서 목록을 직접 변경하지 못하도록 읽기 전용 목록을 반환한다.
     */
    public List<KnightMoveLog> getKnightMoveLog() {
        return Collections.unmodifiableList(knightMoveLog);
    }

    /**
     * 용도: 나이트 이동 한 번 기록.
     * 나이트가 이동할 때마다 호출되어 캡처 여부와 관계없이 이동 좌표를 궤적에 추가한다.
     */
    public void recordKnightMove(int fromX, int fromY, int toX, int toY) {
        knightMoveLog.add(new KnightMoveLog(currentTurn, fromX, fromY, toX, toY));
    }

    /**
     * 용도: 이동 처리 결과 반영.
     * 한 턴의 이동 처리 후 획득 점수, 보드 상태, 진행 상태를 한 번에 갱신한다.
     */
    public void applyMoveResult(int gainedScore, String newFen, GameStatus newStatus) {
        this.score += gainedScore;
        this.fen = newFen;
        this.currentTurn += 1;
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 용도: 목표 점수 달성 여부 확인.
     * 현재 점수가 난이도별 목표 점수 이상인지 확인한다.
     */
    public boolean hasReachedTargetScore() {
        return score >= targetScore;
    }

    /**
     * 용도: 최대 턴 도달 여부 확인.
     * 15턴 종료 조건에 도달했는지 확인한다.
     */
    public boolean hasReachedMaxTurn() {
        return currentTurn >= MAX_TURN;
    }
}