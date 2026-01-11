package uz.fido.pfexchange.serialization;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class DynamicNamingMessageConverter extends MappingJackson2HttpMessageConverter {

    private final NamingStrategyResolver resolver;

    private final Map<PropertyNamingStrategy, ObjectMapper> mapperCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        setObjectMapper(getOrCreateMapper(PropertyNamingStrategies.LOWER_CAMEL_CASE));
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        ObjectMapper mapper = getContextMapper();
        return mapper.readValue(inputMessage.getBody(), mapper.constructType(type));
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return getContextMapper().readValue(inputMessage.getBody(), clazz);
    }

    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        getContextMapper().writeValue(outputMessage.getBody(), object);
    }

    @Override
    public boolean canRead(
            Type type, Class<?> contextClass, org.springframework.http.MediaType mediaType) {
        if (isSwaggerRequest()) {
            return false;
        }
        return super.canRead(type, contextClass, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, org.springframework.http.MediaType mediaType) {
        if (isSwaggerRequest()) {
            return false;
        }
        return super.canWrite(clazz, mediaType);
    }

    private boolean isSwaggerRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String uri = attrs.getRequest().getRequestURI();
                return uri.startsWith("/v3/api-docs")
                        || uri.startsWith("/swagger-ui")
                        || uri.startsWith("/swagger-resources");
            }
        } catch (Exception e) {
            // just process
        }
        return false;
    }

    private ObjectMapper getContextMapper() {
        PropertyNamingStrategy strategy = resolver.resolve();
        return getOrCreateMapper(strategy);
    }

    private ObjectMapper getOrCreateMapper(PropertyNamingStrategy strategy) {
        return mapperCache.computeIfAbsent(strategy, this::buildMapper);
    }

    private ObjectMapper buildMapper(PropertyNamingStrategy strategy) {
        return JsonMapper.builder()
                .propertyNamingStrategy(strategy)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .addModule(new JavaTimeModule())
                .build();
    }
}
