package uz.fido.pfexchange.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import uz.fido.pfexchange.exception.ConversionException;

public final class XmlUtils {

    private static final XmlMapper SNAKE_CASE_MAPPER =
            createMapper(PropertyNamingStrategies.SNAKE_CASE);
    private static final XmlMapper CAMEL_CASE_MAPPER =
            createMapper(PropertyNamingStrategies.LOWER_CAMEL_CASE);

    private XmlUtils() {}

    private static XmlMapper createMapper(PropertyNamingStrategy strategy) {
        return XmlMapper.builder()
                .propertyNamingStrategy(strategy)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(new JavaTimeModule())
                .build();
    }

    public static <T> String toSnakeCaseXml(T dto) {
        return toXml(dto, SNAKE_CASE_MAPPER);
    }

    public static <T> String toCamelCaseXml(T dto) {
        return toXml(dto, CAMEL_CASE_MAPPER);
    }

    public static <T> String dtoToXml(T dto) {
        return toSnakeCaseXml(dto);
    }

    public static <T> T fromSnakeCaseXml(String xml, Class<T> clazz) {
        return fromXml(xml, clazz, SNAKE_CASE_MAPPER);
    }

    public static <T> T fromCamelCaseXml(String xml, Class<T> clazz) {
        return fromXml(xml, clazz, CAMEL_CASE_MAPPER);
    }

    private static <T> String toXml(T dto, XmlMapper mapper) {
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }
        try {
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to convert DTO to XML: " + e.getMessage(), e);
        }
    }

    private static <T> T fromXml(String xml, Class<T> clazz, XmlMapper mapper) {
        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException("XML cannot be null or blank");
        }
        try {
            return mapper.readValue(xml, clazz);
        } catch (JsonProcessingException e) {
            throw new ConversionException("Failed to parse XML to DTO: " + e.getMessage(), e);
        }
    }
}
