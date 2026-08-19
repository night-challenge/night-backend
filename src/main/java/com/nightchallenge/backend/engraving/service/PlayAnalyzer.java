package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.game.domain.GameSession;

/**
 * 용도: 게임 플레이 분석 추상화.
 * 완료된 게임 세션을 분석해 별자리 이름, 키워드, 코멘트를 생성하는 방식을 추상화한다.
 * 구현체를 교체해도(예: 더미 → LLM 연동) 이 인터페이스를 사용하는 코드는 변경할 필요가 없다.
 */
public interface PlayAnalyzer {

    /**
     * 용도: 플레이 분석.
     * 완료된 게임 세션의 진행 내역을 바탕으로 별자리 이름, 키워드 3개, 코멘트를 생성한다.
     */
    PlayAnalysisResult analyze(GameSession session);
}