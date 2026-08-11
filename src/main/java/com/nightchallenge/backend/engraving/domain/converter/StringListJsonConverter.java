package com.nightchallenge.backend.engraving.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

/**
 * 용도: 문자열 목록과 JSON 컬럼 상호 변환.
 * keywords처럼 문자열 목록으로 관리하는 필드를 DB의 JSON 컬럼과 자동으로 변환한다.
 */
@Converter
public class StringListJsonConverter implements AttributeConverter<List<String>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 용도: 엔티티 값을 DB 저장용 JSON 문자열로 변환.
     * 저장 시 keywords 목록을 JSON 배열 문자열로 직렬화한다.
     */
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "keywords 직렬화에 실패했습니다.");
        }
    }

    /**
     * 용도: DB의 JSON 문자열을 엔티티 값으로 변환.
     * 조회 시 JSON 배열 문자열을 keywords 목록으로 역직렬화한다.
     */
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        try {
            return OBJECT_MAPPER.readValue(dbData, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "keywords 역직렬화에 실패했습니다.");
        }
    }
}