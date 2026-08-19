package com.nightchallenge.backend.engraving.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: OpenAI 플레이 분석 프롬프트 검증.
 * AI 추천 각인 이름에 적용되는 형식 규칙이 유지되는지 확인한다.
 */
class OpenAiPlayAnalyzerTest {

    /**
     * 용도: AI 추천 각인 이름 규칙 검증.
     * 실제 별자리 이름을 제외하고 창작한 'OO의 궤적' 형식을 요구하는지 확인한다.
     */
    @Test
    void promptRequiresCreativeTrailName() {
        assertThat(OpenAiPlayAnalyzer.PROMPT_TEMPLATE)
                .contains("실제로 존재하는 별자리 이름은 사용하지 마세요")
                .contains("플레이 성향을 표현하는 창작 이름")
                .contains("'OO의 궤적' 형식")
                .contains("\"constellationName\": \"도전의 궤적\"");
    }
}
