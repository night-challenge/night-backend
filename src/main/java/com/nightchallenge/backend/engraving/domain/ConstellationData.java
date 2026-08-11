package com.nightchallenge.backend.engraving.domain;

/**
 * 용도: 각인 하나의 전체 별자리 데이터 표현.
 * 원본 나이트 이동 궤적인 before와 재구성된 최종 별자리인 after를 함께 담는다.
 */
public record ConstellationData(
        ConstellationShape before,
        ConstellationShape after
) {
}