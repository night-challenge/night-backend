package com.nightchallenge.backend.engraving.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 용도: 별자리 데이터와 JSON 컬럼 상호 변환.
 * before/after 좌표 데이터를 담은 ConstellationData를 DB의 JSON 컬럼과 자동으로 변환한다.
 */
@Converter
public class ConstellationDataJsonConverter implements AttributeConverter<ConstellationData, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 용도: 엔티티 값을 DB 저장용 JSON 문자열로 변환.
     * 저장 시 before/after 좌표 데이터를 JSON 객체 문자열로 직렬화한다.
     */
    @Override
    public String convertToDatabaseColumn(ConstellationData attribute) {
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "constellationData 직렬화에 실패했습니다.");
        }
    }

    /**
     * 용도: DB의 JSON 문자열을 엔티티 값으로 변환.
     * 조회 시 JSON 객체 문자열을 ConstellationData로 역직렬화한다.
     */
    @Override
    public ConstellationData convertToEntityAttribute(String dbData) {
        try {
            return OBJECT_MAPPER.readValue(dbData, ConstellationData.class);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "constellationData 역직렬화에 실패했습니다.");
        }
    }
}