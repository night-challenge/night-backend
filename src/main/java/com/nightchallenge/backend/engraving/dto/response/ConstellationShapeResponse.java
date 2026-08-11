package com.nightchallenge.backend.engraving.dto.response;

import java.util.List;

/**
 * 용도: 별자리 모양 응답.
 * 별자리를 구성하는 점 목록과 점 ID 사이의 연결 관계를 하나의 좌표 데이터로 전달한다.
 */
public record ConstellationShapeResponse(
        List<ConstellationPointResponse> points,
        List<List<Integer>> connections
) {

    /**
     * 용도: 별자리 응답 데이터 보호.
     * 외부에서 전달한 목록을 복사해 DTO 생성 후 점과 연결 목록 자체가 변경되지 않게 한다.
     */
    public ConstellationShapeResponse {
        points = List.copyOf(points);
        connections = connections.stream()
                .map(List::copyOf)
                .toList();
    }
}
