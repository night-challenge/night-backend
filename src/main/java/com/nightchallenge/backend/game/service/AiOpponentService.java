package com.nightchallenge.backend.game.service;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.PieceType;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * 용도: AI 상대의 이동 선택.
 * 완전한 체스 엔진 없이, 사용자의 나이트에게 살짝 유리한 확률적 가중치로 AI의 수를 선택한다.
 */
@Component
public class AiOpponentService {

    private final Random random = new Random();

    /**
     * 용도: AI 이동 선택.
     * 현재 보드에서 합법적인 수 중 하나를 가중치 기반 확률로 선택한다.
     * 사용자 나이트를 위협하는 수는 가중치를 낮추고, AI 기물이 사용자 나이트에게 잡힐 위치로 이동하는 수는 가중치를 높인다.
     */
    public Move selectMove(Board board) {
        List<Move> legalMoves = board.legalMoves();
        if (legalMoves.isEmpty()) {
            return null;
        }

        double[] weights = new double[legalMoves.size()];
        double totalWeight = 0;

        for (int i = 0; i < legalMoves.size(); i++) {
            weights[i] = calculateWeight(board, legalMoves.get(i));
            totalWeight += weights[i];
        }

        return pickWeightedRandom(legalMoves, weights, totalWeight);
    }

    /**
     * 용도: 이동 가중치 계산.
     * 사용자 나이트를 위협하는 수는 가중치를 40%로 낮추고, AI 기물을 사용자 나이트의 공격 범위에
     * 노출시키는 수는 가중치를 160%로 높여 사용자에게 살짝 유리한 방향으로 확률을 조정한다.
     */
    private double calculateWeight(Board board, Move move) {
        double weight = 1.0;

        board.doMove(move);
        try {
            if (threatensUserKnight(board)) {
                weight *= 0.4;
            }
            if (exposesToUserKnightCapture(board)) {
                weight *= 1.6;
            }
        } finally {
            board.undoMove();
        }

        return weight;
    }

    /**
     * 용도: 사용자 나이트 위협 여부 확인.
     * AI 이동 후 보드에서, 사용자(백)의 나이트가 AI(흑)의 다음 수에 잡힐 수 있는 상태인지 확인한다.
     */
    private boolean threatensUserKnight(Board board) {
        return board.legalMoves().stream()
                .anyMatch(nextMove -> board.getPiece(nextMove.getTo()).getPieceType() == PieceType.KNIGHT
                        && board.getPiece(nextMove.getTo()).getPieceSide() == Side.WHITE);
    }

    /**
     * 용도: AI 기물 노출 여부 확인.
     * AI 이동 후 보드에서, 사용자(백)의 나이트가 다음 수에 AI(흑) 기물을 잡을 수 있는 상태인지 확인한다.
     */
    private boolean exposesToUserKnightCapture(Board board) {
        return board.legalMoves().stream()
                .filter(nextMove -> board.getPiece(nextMove.getFrom()).getPieceType() == PieceType.KNIGHT)
                .anyMatch(nextMove -> board.getPiece(nextMove.getTo()).getPieceType() != PieceType.NONE);
    }

    /**
     * 용도: 가중치 기반 무작위 선택.
     * 각 수의 가중치를 확률로 환산해 무작위로 하나를 선택한다.
     */
    private Move pickWeightedRandom(List<Move> moves, double[] weights, double totalWeight) {
        double point = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (int i = 0; i < moves.size(); i++) {
            cumulative += weights[i];
            if (point <= cumulative) {
                return moves.get(i);
            }
        }

        return moves.get(moves.size() - 1);
    }
}