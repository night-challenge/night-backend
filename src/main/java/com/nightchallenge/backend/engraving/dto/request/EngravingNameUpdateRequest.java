package com.nightchallenge.backend.engraving.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 용도: 각인 이름 수정 요청.
 * 수정할 별자리 이름을 전달받고 요청 필드가 누락되지 않았는지 검증한다.
 */
public record EngravingNameUpdateRequest(
        @NotNull(message = "별자리 이름을 입력해 주세요.")
        String constellationName
) {
}
