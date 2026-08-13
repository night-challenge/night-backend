package com.nightchallenge.backend.engraving.service.dto;

/**
 * 용도: OpenAI Chat Completions 메시지 표현.
 * 요청과 응답에서 공통으로 사용하는 role/content 쌍을 나타낸다.
 */
public record OpenAiChatMessage(String role, String content) {
}