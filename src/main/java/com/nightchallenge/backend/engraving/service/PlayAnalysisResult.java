package com.nightchallenge.backend.engraving.service;

import java.util.List;

/**
 * 용도: 플레이 분석 결과 표현.
 * PlayAnalyzer가 생성한 별자리 이름, 키워드, 코멘트를 담는다.
 */
public record PlayAnalysisResult(
        String constellationName,
        List<String> keywords,
        String comment
) {
}