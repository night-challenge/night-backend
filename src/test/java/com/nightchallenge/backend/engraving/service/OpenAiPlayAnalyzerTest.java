package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.game.domain.GameMode;
import com.nightchallenge.backend.game.domain.GameSession;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 용도: OpenAI 플레이 분석 프롬프트 검증.
 * AI 추천 각인 이름에 적용되는 형식 규칙이 유지되는지 확인한다.
 */
class OpenAiPlayAnalyzerTest {

    /**
     * 용도: AI 추천 각인 이름 규칙 검증.
     * 고정 이름 예시 없이 키워드를 기반으로 창작한 '~의 궤적' 형식을 요구하는지 확인한다.
     */
    @Test
    void promptRequiresCreativeTrailName() {
        assertThat(OpenAiPlayAnalyzer.PROMPT_TEMPLATE)
                .contains("실제로 존재하는 별자리 이름은 사용하지 마세요")
                .contains("플레이 성향을 표현하는 창작 이름")
                .contains("키워드 3개를 먼저 선정")
                .contains("선정한 키워드 3개를 바탕으로 각인 이름과 코멘트를 생성")
                .contains("앞부분의 글자 수는 제한하지 않되")
                .contains("'의 궤적'으로 끝나게")
                .contains("나이트 이동 경로")
                .contains("keywords, constellationName, comment")
                .doesNotContain("도전의 궤적");
    }

    /**
     * 용도: AI 분석 키워드 형식 규칙 검증.
     * 완성된 키워드 예시에 의존하지 않고 짧은 단어 형태와 실제 플레이 반영을 요구하는지 확인한다.
     */
    @Test
    void promptRequiresShortKeywordsWithoutFixedExamples() {
        assertThat(OpenAiPlayAnalyzer.PROMPT_TEMPLATE)
                .contains("한글 2~3자의 짧은 단어")
                .contains("띄어쓰기 없는 명사 또는 형용사 형태")
                .contains("문장이나 구절 형태로 작성하지 마세요")
                .contains("실제 게임 정보와 나이트 이동 경로에서 드러난 플레이 특성")
                .contains("동일한 일반 키워드만 반복적으로 선택하지 마세요")
                .doesNotContain("공격적", "민첩함", "전략적", "신속", "창의적");
    }

    /**
     * 용도: 나이트 이동 경로 프롬프트 반영 검증.
     * 기록된 사용자 나이트 이동이 원래 순서와 좌표를 유지한 채 프롬프트에 포함되는지 확인한다.
     */
    @Test
    void buildPromptIncludesKnightMovesInOrder() {
        GameSession session = new GameSession(1L, GameMode.EASY, "initial-fen");
        session.recordKnightMove(1, 0, 2, 2);
        session.recordKnightMove(6, 0, 5, 2);

        OpenAiPlayAnalyzer analyzer = new OpenAiPlayAnalyzer("test-api-key", "test-model");

        String prompt = analyzer.buildPrompt(session);

        assertThat(prompt)
                .contains("나이트 이동 횟수: 2회")
                .containsSubsequence(
                        "1번째 이동: (1,0) → (2,2)",
                        "2번째 이동: (6,0) → (5,2)"
                );
    }
}
