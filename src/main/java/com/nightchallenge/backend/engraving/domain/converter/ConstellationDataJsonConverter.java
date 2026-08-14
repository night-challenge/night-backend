package com.nightchallenge.backend.engraving.domain.converter;

import com.nightchallenge.backend.engraving.domain.ConstellationData;
import com.nightchallenge.backend.global.exception.BusinessException;
import com.nightchallenge.backend.global.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class ConstellationDataJsonConverter implements AttributeConverter<ConstellationData, String> {

    private static final JsonMapper JSON_MAPPER = JsonMapper.shared();

    @Override
    public String convertToDatabaseColumn(ConstellationData attribute) {
        try {
            return JSON_MAPPER.writeValueAsString(attribute);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "constellationData serialize failed");
        }
    }

    @Override
    public ConstellationData convertToEntityAttribute(String dbData) {
        try {
            return JSON_MAPPER.readValue(dbData, ConstellationData.class);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "constellationData deserialize failed");
        }
    }
}