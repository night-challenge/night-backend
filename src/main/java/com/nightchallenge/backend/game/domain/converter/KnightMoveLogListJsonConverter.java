package com.nightchallenge.backend.game.domain.converter;

import com.nightchallenge.backend.game.domain.KnightMoveLog;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * 용도: 나이트 이동 기록 목록과 JSON 컬럼 상호 변환.
 * 매 턴 기록되는 나이트 이동 로그를 DB의 JSON 컬럼과 자동으로 변환한다.
 */
@Converter
public class KnightMoveLogListJsonConverter implements AttributeConverter<List<KnightMoveLog>, String> {

    private static final JsonMapper JSON_MAPPER = JsonMapper.shared();

    /**
     * 용도: 엔티티 값을 DB 저장용 JSON 문자열로 변환.
     * 저장 시 나이트 이동 기록 목록을 JSON 배열 문자열로 직렬화한다.
     */
    @Override
    public String convertToDatabaseColumn(List<KnightMoveLog> attribute) {
        try {
            return JSON_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "knightMoveLog 직렬화에 실패했습니다.");
        }
    }

    /**
     * 용도: DB의 JSON 문자열을 엔티티 값으로 변환.
     * 조회 시 JSON 배열 문자열을 나이트 이동 기록 목록으로 역직렬화한다.
     */
    @Override
    public List<KnightMoveLog> convertToEntityAttribute(String dbData) {
        try {
            return JSON_MAPPER.readValue(dbData, new TypeReference<List<KnightMoveLog>>() {});
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "knightMoveLog 역직렬화에 실패했습니다.");
        }
    }
}