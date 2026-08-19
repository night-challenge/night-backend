package com.nightchallenge.backend.engravingrequest.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * 용도: 각인 신청 상태 구분.
 * 신청 완료와 취소 상태를 목록 필터링과 이후 저장 데이터에서 동일한 기준으로 관리한다.
 */
public enum EngravingRequestStatus {

    COMPLETED("신청완료"),
    CANCELED("취소됨");

    private final String value;

    EngravingRequestStatus(String value) {
        this.value = value;
    }

    /**
     * 용도: API 상태값 반환.
     * enum 값을 명세에 정의된 한글 상태 문자열로 변환한다.
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 용도: API 상태값 변환.
     * 요청으로 전달된 한글 상태 문자열을 대응하는 enum 값으로 변환한다.
     */
    @JsonCreator
    public static EngravingRequestStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 신청 상태입니다."));
    }
}
