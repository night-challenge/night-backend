package com.nightchallenge.backend.engraving.service.dto;

import java.util.List;

/**
 * 용도: OpenAI Chat Completions 응답 본문.
 * 응답 후보 목록(choices)만 필요한 만큼 매핑한다.
 */
public record OpenAiChatResponse(List<Choice> choices) {

    public record Choice(OpenAiChatMessage message) {
    }
}