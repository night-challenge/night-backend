package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import com.nightchallenge.backend.game.repository.GameSessionRepository;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 용도: 게임 세션 생성과 조회.
 * 새 게임 시작, 진행 중인 게임 조회, 합법적인 이동 조회, 누적 통계 조회 등
 * 게임 진행 자체와 관련된 비즈니스 로직을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    /**
     * 로그인 기능 도입 전까지 사용하는 고정 사용자 식별자.
     */
    private static final Long TEMP_USER_ID = 1L;

    private final GameSessionRepository gameSessionRepository;

    /**
     * 용도: 새 게임 시작.
     * 선택한 난이도로 표준 초기 배치의 체스 보드를 가진 게임 세션을 생성한다.
     */
    @Transactional
    public GameSession startGame(GameMode mode) {
        Board initialBoard = new Board();
        GameSession session = new GameSession(TEMP_USER_ID, mode, initialBoard.getFen());

        return gameSessionRepository.save(session);
    }

    /**
     * 용도: 진행 중인 게임 조회.
     * 화면 2.1(재진입)에서 이어할 게임이 있는지 확인할 때 사용하며, 없으면 404 비즈니스 예외를 발생시킨다.
     */
    public GameSession getActiveGame() {
        return gameSessionRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDescIdDesc(TEMP_USER_ID, GameStatus.IN_PROGRESS)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND, "진행 중인 게임이 없습니다."));
    }

    /**
     * 용도: 게임 세션 단건 조회.
     * id와 일치하는 게임 세션을 조회하고 없으면 404 비즈니스 예외를 발생시킨다.
     */
    public GameSession getGameSession(Long gameSessionId) {
        return gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND));
    }

    /**
     * 용도: 특정 칸의 합법적인 이동 가능 칸 조회.
     * 화면 3.1(말 선택)에서 선택한 말이 이동 가능한 칸을 하이라이트할 때 사용한다.
     * 빈 칸이거나 이동 가능한 수가 없는 기물을 선택한 경우 빈 목록을 반환한다.
     */
    public List<String> getLegalMoves(Long gameSessionId, String squareName) {
        GameSession session = getGameSession(gameSessionId);
        Square square = parseSquare(squareName);

        Board board = new Board();
        board.loadFromFen(session.getFen());

        return board.legalMoves().stream()
                .filter(move -> move.getFrom() == square)
                .map(move -> move.getTo().name())
                .toList();
    }

    /**
     * 용도: 게임 누적 통계 조회.
     * 화면 2.1(재진입)에 표시할 최고 점수와 완료된 게임 총 플레이 횟수를 집계한다.
     * 진행 중인 게임은 결과가 확정되지 않았으므로 집계에서 제외한다.
     */
    public GameStats getStats() {
        long playCount = gameSessionRepository.countByUserIdAndStatusNot(TEMP_USER_ID, GameStatus.IN_PROGRESS);
        int bestScore = gameSessionRepository
                .findTopByUserIdAndStatusNotOrderByScoreDesc(TEMP_USER_ID, GameStatus.IN_PROGRESS)
                .map(GameSession::getScore)
                .orElse(0);

        return new GameStats(bestScore, playCount);
    }

    /**
     * 용도: 좌표 문자열 파싱.
     * "e2"와 같은 좌표 문자열을 체스 칸 객체로 변환하며, 형식이 올바르지 않으면 400 비즈니스 예외를 발생시킨다.
     */
    private Square parseSquare(String squareName) {
        try {
            return Square.valueOf(squareName.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "올바르지 않은 좌표입니다.");
        }
    }
}
