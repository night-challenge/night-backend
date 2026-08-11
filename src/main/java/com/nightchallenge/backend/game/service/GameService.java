package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.Board;
import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import com.nightchallenge.backend.game.repository.GameSessionRepository;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 용도: 게임 세션 생성과 조회.
 * 새 게임 시작, 진행 중인 게임 조회 등 게임 진행 자체와 관련된 비즈니스 로직을 담당한다.
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
        return gameSessionRepository.findByUserIdAndStatus(TEMP_USER_ID, GameStatus.IN_PROGRESS)
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
}