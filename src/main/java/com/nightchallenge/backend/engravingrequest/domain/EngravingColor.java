package com.nightchallenge.backend.engravingrequest.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 용도: 각인 색상 구분.
 * 신청 API와 저장 데이터에서 사용하는 금색, 은색, 검은색 값을 동일한 기준으로 관리한다.
 */
public enum EngravingColor {

    GOLD("gold"),
    SILVER("silver"),
    BLACK("black");

    private final String value;

    EngravingColor(String value) {
        this.value = value;
    }

    /**
     * 용도: API 색상값 반환.
     * enum 값을 명세에 정의된 소문자 JSON 문자열로 변환한다.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 용도: API 색상값 변환.
     * 요청 JSON의 소문자 색상 문자열을 대응하는 enum 값으로 변환한다.
     */
    @JsonCreator
    public static EngravingColor fromValue(String value) {
        return Arrays.stream(values())
                .filter(color -> color.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 각인 색상입니다."));
    }
}
