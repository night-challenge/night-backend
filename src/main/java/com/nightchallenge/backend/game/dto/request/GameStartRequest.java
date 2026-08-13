package com.nightchallenge.backend.game.dto.request;

import com.nightchallenge.backend.game.domain.GameMode;
import jakarta.validation.constraints.NotNull;

/**
 * 용도: 게임 시작 요청.
 * 사용자가 선택한 난이도를 전달받아 새 게임 세션을 생성할 때 사용한다.
 */
public record GameStartRequest(
        @NotNull(message = "게임 난이도를 선택해 주세요.")
        GameMode mode
) {
}