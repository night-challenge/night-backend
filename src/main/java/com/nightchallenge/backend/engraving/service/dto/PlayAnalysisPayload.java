package com.nightchallenge.backend.engraving.service.dto;

import java.util.List;

/**
 * 용도: LLM이 생성한 플레이 분석 결과의 원시 JSON 페이로드.
 * OpenAI 응답의 content 문자열(JSON)을 그대로 매핑하기 위한 구조다.
 */
public record PlayAnalysisPayload(
        String constellationName,
        List<String> keywords,
        String comment
) {
}