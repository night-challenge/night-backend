package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.game.domain.GameSession;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 용도: PlayAnalyzer 임시 구현체.
 * 실제 AI 분석(LLM 연동) 이전까지 고정된 형식의 결과를 반환해 전체 파이프라인이 동작하도록 한다.
 * 추후 LlmPlayAnalyzer 등으로 교체될 예정이며, 교체 시 PlayAnalyzer를 사용하는 다른 코드는 변경하지 않는다.
 */
@Component
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