package com.nightchallenge.backend.engraving.dto.response;

/**
 * 용도: 각인 상세 별자리 응답.
 * 원본 나이트 이동 기록인 before와 재구성된 최종 별자리인 after를 구분해 전달한다.
 */
public record ConstellationDataResponse(
        ConstellationShapeResponse before,
        ConstellationShapeResponse after
) {
}
