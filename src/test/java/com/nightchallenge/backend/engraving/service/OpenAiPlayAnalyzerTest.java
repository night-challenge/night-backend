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
                .contains("'의 궤적' 앞부분은 한글 2~4자의 자연스러운 명사 또는 명사형 표현")
                .contains("하나의 의미로 자연스럽게 읽히는 표현")
                .contains("서로 다른 단어를 억지로 결합한 표현이나 띄어쓰기가 포함된 구절은 사용하지 마세요")
                .contains("완성된 이름이 '의 궤적'과 자연스럽게 연결")
                .contains("'의 궤적'으로 끝나게")
                .contains("기물명이나 게임 용어를 이름에 그대로 사용하지 마세요")
                .contains("'나이트', '체스', '게임', '이동', '경로'")
                .contains("선정한 플레이 성향 키워드를 바탕으로 이름을 창작")
                .contains("플레이 성향 표현에 특정 단어를 금지어로 지정하지 마세요")
                .contains("입력과 무관한 일반적인 이름을 습관적으로 반복하지 마세요")
                .contains("게임 정보, 점수, 턴 수와 나이트 이동 경로에서 확인되는 특징")
                .contains("서로 다른 게임 정보와 이동 경로에는 가능한 한 서로 다른 어휘와 표현")
                .contains("나이트 이동 경로")
                .contains("keywords, constellationName, comment")
                .doesNotContain("도전의 궤적", "나이트의 궤적");
    }

    /**
     * 용도: AI 분석 코멘트 형식 규칙 검증.
     * 코멘트가 충분한 설명을 담은 한 문장의 존댓말로 생성되도록 요구하는지 확인한다.
     */
    @Test
    void promptRequiresDescriptivePoliteComment() {
        assertThat(OpenAiPlayAnalyzer.PROMPT_TEMPLATE)
                .contains("완료된 게임의 플레이를 돌아보며 플레이어에게 직접 이야기하는 자연스러운 과거형 존댓말 한 문장")
                .contains("반드시 '당신은'으로 시작")
                .contains("현재형인 '~합니다.'로 끝내지 마세요")
                .contains("'~했습니다.' 하나로만 제한하지 말고 자연스러운 과거형 존댓말로 끝내세요")
                .contains("공백과 문장부호를 포함해 최소 35자 이상, 최대 50자 이하")
                .contains("게임 정보와 나이트 이동 경로를 바탕으로 플레이 성향을 자연스럽게 해석")
                .contains("명사형 또는 지나치게 짧은 문장으로 작성하지 마세요");
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
