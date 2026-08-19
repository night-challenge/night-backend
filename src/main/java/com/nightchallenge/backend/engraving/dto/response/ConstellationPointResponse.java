package com.nightchallenge.backend.engraving.dto.response;

/**
 * 용도: 별자리 점 좌표 응답.
 * 점 식별자와 좌표를 전달해 프론트가 별자리의 점과 연결선을 렌더링할 수 있게 한다.
 */
public record ConstellationPointResponse(
        int id,
        int x,
        int y
) {

    /**
     * 용도: 별자리 점 DTO 변환.
     * 도메인의 ConstellationPoint를 API 명세의 응답으로 변환한다.
     */
    public static ConstellationPointResponse from(com.nightchallenge.backend.engraving.domain.ConstellationPoint point) {
        return new ConstellationPointResponse(point.id(), point.x(), point.y());
    }
}