package com.nightchallenge.backend.engraving.dto.response;

/**
 * 용도: 각인 이름 수정 응답.
 * 수정된 각인의 식별자와 별자리 이름을 이름 수정 성공 결과로 전달한다.
 */
public record EngravingNameUpdateResponse(
        Long id,
        String constellationName
) {
}
