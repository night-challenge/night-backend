package com.nightchallenge.backend.engraving.domain;

/**
 * 용도: 별자리 점 좌표 표현.
 * before(게임판 격자 좌표)와 after(300x300 캔버스 좌표)에서 공통으로 쓰는 점 하나를 나타낸다.
 */
public record ConstellationPoint(
        int id,
        int x,
        int y
) {
}