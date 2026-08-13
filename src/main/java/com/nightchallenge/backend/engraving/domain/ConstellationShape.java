package com.nightchallenge.backend.engraving.domain;

import java.util.List;

/**
 * 용도: 별자리 하나의 좌표 데이터 표현.
 * 점 목록과 점 ID 사이의 연결 관계를 함께 담아 before 또는 after 데이터를 구성한다.
 */
public record ConstellationShape(
        List<ConstellationPoint> points,
        List<List<Integer>> connections
) {

    /**
     * 용도: 별자리 데이터 보호.
     * 외부에서 전달한 목록을 복사해 생성 후 점과 연결 목록 자체가 변경되지 않게 한다.
     */
    public ConstellationShape {
        points = List.copyOf(points);
        connections = connections.stream()
                .map(List::copyOf)
                .toList();
    }
}