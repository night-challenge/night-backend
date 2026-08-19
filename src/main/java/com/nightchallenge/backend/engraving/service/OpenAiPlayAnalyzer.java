package com.nightchallenge.backend.engraving.service;

import com.nightchallenge.backend.engraving.service.dto.OpenAiChatMessage;
import com.nightchallenge.backend.engraving.service.dto.OpenAiChatRequest;
import com.nightchallenge.backend.engraving.service.dto.OpenAiChatResponse;
import com.nightchallenge.backend.engraving.service.dto.PlayAnalysisPayload;
import com.nightchallenge.backend.game.domain.GameSession;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * 용도: PlayAnalyzer의 OpenAI 연동 구현체.
 * 완료된 게임 세션 정보를 바탕으로 OpenAI Chat Completions API를 호출해
 * 별자리 이름, 키워드 3개, 코멘트를 생성한다.
 */
@Component
public class OpenAiPlayAnalyzer implements PlayAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(OpenAiPlayAnalyzer.class);
    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final JsonMapper JSON_MAPPER = JsonMapper.shared();

    static final String PROMPT_TEMPLATE = """
            당신은 체스 미니게임의 플레이 스타일을 분석하는 어시스턴트입니다.
            아래 게임 정보를 참고해 이 플레이어의 별자리 이름, 키워드 3개, 한 문장 코멘트를 만들어주세요.

            - 난이도: %s
            - 최종 점수: %d점 (목표 %d점)
            - 총 턴 수: %d턴
            - 나이트 이동 횟수: %d회

            별자리 이름은 다음 규칙을 모두 지켜 작성하세요.
            - 양자리, 사자자리 등 실제로 존재하는 별자리 이름은 사용하지 마세요.
            - 플레이 성향을 표현하는 창작 이름을 사용하세요.
            - 반드시 짧고 자연스러운 'OO의 궤적' 형식으로 작성하세요.

            반드시 아래 JSON 형식으로만 응답하세요. 다른 설명은 포함하지 마세요.
            {"constellationName": "도전의 궤적", "keywords": ["키워드1", "키워드2", "키워드3"], "comment": "당신은 ~했습니다 형태의 한 문장"}
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiPlayAnalyzer(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.model:gpt-4o-mini}") String model
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.restClient = RestClient.create();
    }

    /**
     * 용도: OpenAI를 이용한 플레이 분석.
     * 게임 세션 정보로 프롬프트를 구성해 OpenAI에 요청하고, JSON 응답을 분석 결과로 변환한다.
     */
    @Override
    public PlayAnalysisResult analyze(GameSession session) {
        OpenAiChatRequest request = buildRequest(session);

        OpenAiChatResponse response;
        try {
            response = restClient.post()
                    .uri(CHAT_COMPLETIONS_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OpenAiChatResponse.class);
        } catch (Exception exception) {
            log.error("OpenAI 분석 요청 실패", exception);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 분석 요청에 실패했습니다.");
        }

        return toResult(response);
    }

    /**
     * 용도: OpenAI 요청 객체 구성.
     * 게임 정보를 담은 프롬프트와 JSON 강제 응답 형식을 포함한 요청을 만든다.
     */
    private OpenAiChatRequest buildRequest(GameSession session) {
        int knightMoveCount = session.getKnightMoveLog().size();
        String prompt = PROMPT_TEMPLATE.formatted(
                session.getMode(),
                session.getScore(),
                session.getTargetScore(),
                session.getCurrentTurn(),
                knightMoveCount
        );

        return new OpenAiChatRequest(
                model,
                List.of(new OpenAiChatMessage("user", prompt)),
                0.8,
                OpenAiChatRequest.ResponseFormat.jsonObject()
        );
    }

    /**
     * 용도: OpenAI 응답을 분석 결과로 변환.
     * 응답 content에 담긴 JSON 문자열을 파싱해 별자리 이름, 키워드, 코멘트로 구성된 결과를 만든다.
     */
    private PlayAnalysisResult toResult(OpenAiChatResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 분석 응답이 비어 있습니다.");
        }

        String content = response.choices().get(0).message().content();

        try {
            PlayAnalysisPayload payload = JSON_MAPPER.readValue(content, PlayAnalysisPayload.class);
            return new PlayAnalysisResult(payload.constellationName(), payload.keywords(), payload.comment());
        } catch (Exception exception) {
            log.error("OpenAI 분석 응답 처리 실패", exception);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 분석 결과 처리에 실패했습니다.");
        }
    }
}
