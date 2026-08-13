package com.nightchallenge.backend.game.dto.response;

import java.util.List;

/**
 * 용도: 특정 칸의 합법적인 이동 가능 칸 목록 응답.
 * 화면 3.1(말 선택)에서 선택한 말이 이동 가능한 칸을 하이라이트할 때 사용한다.
 */
public record LegalMovesResponse(
        String from,
        List<String> legalMoves
) {

    public LegalMovesResponse {
        legalMoves = List.copyOf(legalMoves);
    }
}