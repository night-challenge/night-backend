package com.nightchallenge.backend.engraving.service.dto;

import java.util.List;

/**
 * 용도: OpenAI Chat Completions 요청 본문.
 * 사용할 모델, 대화 메시지, response_format(JSON 강제) 설정을 담는다.
 */
public record OpenAiChatRequest(
        String model,
        List<OpenAiChatMessage> messages,
        double temperature,
        ResponseFormat response_format
) {

    /**
     * 용도: 응답 형식 강제 지정.
     * OpenAI가 순수 JSON 객체로만 응답하도록 강제한다.
     */
    public record ResponseFormat(String type) {

        public static ResponseFormat jsonObject() {
            return new ResponseFormat("json_object");
        }
    }
}