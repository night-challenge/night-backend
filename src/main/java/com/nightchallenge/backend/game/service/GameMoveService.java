package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.game.domain.GameStatus;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 용도: 게임 한 턴의 이동 처리.
 * 사용자 이동을 검증하고 실행한 뒤 점수와 궤적을 갱신하고, 게임이 계속되면 AI의 응수까지 처리한다.
 * "1턴"은 사용자 이동 1회와 이어지는 AI 응수 1회를 합친 단위로 취급하며, 턴 수 증가는 사용자 이동에서만 일어난다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GameMoveService {

    private final ScoreTable scoreTable;
    private final AiOpponentService aiOpponentService;

    /**
     * 용도: 한 턴 전체 처리.
     * 사용자 이동을 검증 및 실행하고, 게임이 진행 중이면 이어서 AI의 응수까지 처리한 뒤 최종 결과를 반환한다.
     */
    public MoveResult processTurn(GameSession session, String fromSquare, String toSquare) {
        if (session.getStatus() != GameStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.GAME_ALREADY_FINISHED);
        }

        Board board = new Board();
        board.loadFromFen(session.getFen());

        Move userMove = createMove(fromSquare, toSquare);
        MoveOutcome userOutcome = applyMove(board, session, userMove, true);

        // 승패 판정은 이번 이동으로 갱신될 점수·턴(projected 값)을 기준으로 미리 계산한다.
        // session.applyUserMoveResult 호출 전에는 아직 이번 이동의 점수/턴이 반영되지 않아,
        // determineStatus를 session의 현재 상태만으로 계산하면 한 수 지연된 판정이 나오기 때문이다.
        int projectedScore = session.getScore() + userOutcome.gainedScore();
        int projectedTurn = session.getCurrentTurn() + 1;
        GameStatus userMoveStatus = determineStatus(session.getTargetScore(), projectedScore, projectedTurn, board);

        session.applyUserMoveResult(userOutcome.gainedScore(), board.getFen(), userMoveStatus);

        MoveOutcome aiOutcome = null;
        if (session.getStatus() == GameStatus.IN_PROGRESS) {
            Move aiMove = aiOpponentService.selectMove(board);
            if (aiMove != null) {
                aiOutcome = applyMove(board, session, aiMove, false);

                // AI 응수는 점수·턴을 바꾸지 않으므로, 이미 갱신된 session의 현재 값을 그대로 사용한다.
                GameStatus aiMoveStatus = determineStatus(session.getTargetScore(), session.getScore(), session.getCurrentTurn(), board);
                session.applyAiMoveResult(board.getFen(), aiMoveStatus);
            }
        }

        return new MoveResult(session, userOutcome, aiOutcome);
    }

    /**
     * 용도: 이동 문자열 파싱.
     * "e2", "e4"와 같은 좌표 문자열을 체스 기물 이동 객체로 변환한다.
     */
    private Move createMove(String fromSquare, String toSquare) {
        try {
            Square from = Square.valueOf(fromSquare.toUpperCase());
            Square to = Square.valueOf(toSquare.toUpperCase());
            return new Move(from, to);
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_MOVE);
        }
    }

    /**
     * 용도: 보드에 이동 한 번 적용.
     * 합법적인 이동인지 검증한 뒤 실행하고, 나이트로 캡처했을 때만 점수를 계산한다.
     * 나이트 이동 궤적은 사용자의 이동일 때만 기록하며, AI의 나이트 이동은 궤적에 포함하지 않는다.
     * 빈 칸 여부는 Piece.NONE과의 동등 비교로 판단한다. Piece.NONE.getPieceType()은 null을 반환하므로
     * PieceType과 직접 비교하면 안 된다.
     */
    private MoveOutcome applyMove(Board board, GameSession session, Move move, boolean isUserMove) {
        if (!board.legalMoves().contains(move)) {
            throw new BusinessException(ErrorCode.INVALID_MOVE);
        }

        Piece movingPiece = board.getPiece(move.getFrom());
        Piece capturedPiece = board.getPiece(move.getTo());
        boolean isCapture = !capturedPiece.equals(Piece.NONE);
        boolean isKnightMove = movingPiece.getPieceType() == PieceType.KNIGHT;

        board.doMove(move);

        int gainedScore = 0;
        PieceType capturedPieceType = null;
        if (isCapture) {
            capturedPieceType = capturedPiece.getPieceType();
            if (isKnightMove && isUserMove) {
                gainedScore = scoreTable.getPoint(session.getMode(), capturedPieceType);
            }
        }

        if (isKnightMove && isUserMove) {
            session.recordKnightMove(
                    move.getFrom().getFile().ordinal(),
                    move.getFrom().getRank().ordinal(),
                    move.getTo().getFile().ordinal(),
                    move.getTo().getRank().ordinal()
            );
        }

        return new MoveOutcome(move, capturedPieceType, gainedScore);
    }

    /**
     * 용도: 이동 후 게임 상태 판정.
     * 목표 점수 달성이나 15턴 종료를 체크메이트보다 우선 판정하고, 그다음 체크메이트 여부를 확인한다.
     */
    /**
     * 용도: 이동 후 게임 상태 판정.
     * 15턴 종료 조건과 목표 점수 달성 조건이 동시에 충족되면 15턴 종료 조건을 우선 판정하고,
     * 그다음 목표 점수 달성, 마지막으로 체크메이트 여부를 확인한다.
     * 아직 엔티티에 반영되지 않은 이동 결과까지 반영한 예상 점수·턴 값을 받아 판정하므로 지연 없이 정확하다.
     */
    private GameStatus determineStatus(int targetScore, int projectedScore, int projectedTurn, Board board) {
        if (projectedTurn >= GameSession.MAX_TURN) {
            return GameStatus.LOST;
        }
        if (projectedScore >= targetScore) {
            return GameStatus.WON;
        }
        if (board.isMated()) {
            return GameStatus.LOST;
        }
        return GameStatus.IN_PROGRESS;
    }

    /**
     * 용도: 한 번의 기물 이동 결과 표현.
     * 실행된 이동, 잡힌 기물 종류(캡처가 없으면 null), 획득 점수를 담는다.
     */
    public record MoveOutcome(Move move, PieceType capturedPieceType, int gainedScore) {
    }

    /**
     * 용도: 한 턴 처리 결과 표현.
     * 갱신된 게임 세션과 사용자 이동, AI 응수(게임이 종료됐다면 null) 결과를 함께 담는다.
     */
    public record MoveResult(GameSession session, MoveOutcome userOutcome, MoveOutcome aiOutcome) {
    }
}