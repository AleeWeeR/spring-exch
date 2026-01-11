package uz.fido.pfexchange.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uz.fido.pfexchange.exception.ConversionException;

public final class JsonUtils {

    private static final ObjectMapper SNAKE_CASE_MAPPER = createMapper(PropertyNamingStrategies.SNAKE_CASE);
    private static final ObjectMapper CAMEL_CASE_MAPPER = createMapper(PropertyNamingStrategies.LOWER_CAMEL_CASE);

    private JsonUtils() {}

    private static ObjectMapper createMapper(PropertyNamingStrategy strategy) {
        return JsonMapper.builder()
                .propertyNamingStrategy(strategy)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(new JavaTimeModule())
                .build();
    }

    public static <T> T fromSnakeCaseJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return SNAKE_CASE_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    public static <T> String toSnakeCaseJson(T dto) {
        if (dto == null) {
            return null;
        }
        try {
            return SNAKE_CASE_MAPPER.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to convert to JSON: " + e.getMessage(), e);
        }
    }

    public static <T> T fromCamelCaseJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return CAMEL_CASE_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

    public static <T> String toCamelCaseJson(T dto) {
        if (dto == null) {
            return null;
        }
        try {
            return CAMEL_CASE_MAPPER.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to convert to JSON: " + e.getMessage(), e);
        }
    }
}