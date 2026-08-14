package com.nightchallenge.backend.game.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 용도: 이동 요청.
 * 사용자가 선택한 출발/도착 칸을 전달받아 한 턴의 이동을 처리할 때 사용한다.
 */
public record GameMoveRequest(
        @NotBlank(message = "출발 칸을 선택해 주세요.")
        String from,

        @NotBlank(message = "도착 칸을 선택해 주세요.")
        String to
) {
}