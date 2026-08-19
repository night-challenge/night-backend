package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.game.domain.GameSession;

import java.util.List;

/**
 * 용도: PlayAnalyzer 테스트용 구현체.
 * OpenAI API 키가 없거나 실제 호출 없이 개발/테스트하고 싶을 때 수동으로 갈아끼워 사용한다.
 * 현재는 OpenAiPlayAnalyzer가 @Component로 등록되어 실제 사용되는 구현체이며,
 * 이 클래스는 @Component가 아니므로 Spring 빈으로 등록되지 않는다.
 */
public class DummyPlayAnalyzer implements PlayAnalyzer {

    @Override
    public PlayAnalysisResult analyze(GameSession session) {
        return new PlayAnalysisResult(
                "나의 별자리",
                List.of("도전", "집중", "성장"),
                "당신은 신중하게 상황을 살피며 기회를 노리는 플레이를 선택했습니다."
        );
    }
}