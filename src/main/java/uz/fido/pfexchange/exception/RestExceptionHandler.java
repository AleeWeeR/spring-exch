package uz.fido.pfexchange.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.*;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import uz.fido.pfexchange.dto.FieldErrorDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.serialization.NamingStrategyResolver;
import uz.fido.pfexchange.utils.ResponseBuilder;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(value = Integer.MIN_VALUE)
public class RestExceptionHandler {

    private final MessageSource messageSource;
    private final NamingStrategyResolver namingResolver;

    @ExceptionHandler(RestException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleRestException(RestException ex) {
        log.error("RestException: {}", ex.getMessage());
        return ResponseBuilder.getError(
                ex.getResponseWrapperDto().getData(),
                ex.getStatus(),
                ex.getResponseWrapperDto().getCode(),
                ex.getResponseWrapperDto().getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleBeanValidation(
            MethodArgumentNotValidException ex, Locale locale) {

        List<FieldErrorDto> errors = extractFieldErrors(ex.getBindingResult(), locale);
        String message = messageSource.getMessage("validation.failed", null, locale);

        log.warn("Bean validation failed: {} error(s)", errors.size());
        return ResponseBuilder.getError(HttpStatus.BAD_REQUEST, message, errors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleBindException(
            BindException ex, Locale locale) {

        List<FieldErrorDto> errors = extractFieldErrors(ex.getBindingResult(), locale);
        String message = messageSource.getMessage("validation.failed", null, locale);

        log.warn("Bind validation failed: {} error(s)", errors.size());
        return ResponseBuilder.getError(HttpStatus.BAD_REQUEST, message, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleJsonErrors(
            HttpMessageNotReadableException ex, Locale locale) {

        Throwable cause = ex.getCause();

        if (cause instanceof JsonMappingException jme && !jme.getPath().isEmpty()) {
            FieldErrorDto fieldError = extractJsonMappingError(jme, locale);
            String message = messageSource.getMessage("validation.failed", null, locale);
            return ResponseBuilder.getError(HttpStatus.BAD_REQUEST, message, List.of(fieldError));
        }

        String messageKey =
                (cause instanceof JsonParseException)
                        ? "validation.malformedJson"
                        : "validation.unreadableBody";
        String message = messageSource.getMessage(messageKey, null, locale);

        return ResponseBuilder.getError(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleMissingParam(
            MissingServletRequestParameterException ex, Locale locale) {

        String fieldName = namingResolver.convertFieldName(ex.getParameterName());
        String errorMsg =
                messageSource.getMessage(
                        "validation.requiredParam", new Object[] {fieldName}, locale);

        FieldErrorDto fieldError = new FieldErrorDto(fieldName, List.of(errorMsg));
        String message = messageSource.getMessage("validation.failed", null, locale);

        return ResponseBuilder.getError(HttpStatus.BAD_REQUEST, message, List.of(fieldError));
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleTypeMismatch(
            TypeMismatchException ex, Locale locale) {

        String fieldName =
                ex.getPropertyName() != null
                        ? namingResolver.convertFieldName(ex.getPropertyName())
                        : "unknown";
        String expectedType =
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";

        String errorMsg =
                messageSource.getMessage(
                        "validation.typeMismatch", new Object[] {fieldName, expectedType}, locale);

        FieldErrorDto fieldError = new FieldErrorDto(fieldName, List.of(errorMsg));
        String message = messageSource.getMessage("validation.failed", null, locale);

        return ResponseBuilder.getError(HttpStatus.BAD_REQUEST, message, List.of(fieldError));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, Locale locale) {

        String message =
                messageSource.getMessage(
                        "http.methodNotSupported", new Object[] {ex.getMethod()}, locale);
        log.warn("Method not supported: {}", ex.getMethod());
        return ResponseBuilder.getError(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, Locale locale) {

        String message =
                messageSource.getMessage(
                        "http.mediaTypeNotSupported", new Object[] {ex.getContentType()}, locale);
        log.warn("Media type not supported: {}", ex.getContentType());
        return ResponseBuilder.getError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, Locale locale) {

        String message = messageSource.getMessage("http.mediaTypeNotAcceptable", null, locale);
        log.warn("Media type not acceptable");
        return ResponseBuilder.getError(HttpStatus.NOT_ACCEPTABLE, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleDataIntegrity(
            DataIntegrityViolationException ex, Locale locale) {

        String message = messageSource.getMessage("data.integrityViolation", null, locale);
        log.error("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());

        return ResponseBuilder.getError(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleEmptyResult(
            EmptyResultDataAccessException ex, Locale locale) {

        String message = messageSource.getMessage("data.notFound", null, locale);
        log.warn("Empty result: {}", ex.getMessage());

        return ResponseBuilder.getError(HttpStatus.NOT_FOUND, message);
    }

    private List<FieldErrorDto> extractFieldErrors(
            org.springframework.validation.BindingResult bindingResult, Locale locale) {

        return bindingResult.getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .collect(
                        Collectors.groupingBy(
                                e -> namingResolver.convertFieldName(e.getField()),
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        e -> messageSource.getMessage(e, locale),
                                        Collectors.toList())))
                .entrySet()
                .stream()
                .map(e -> new FieldErrorDto(e.getKey(), e.getValue()))
                .toList();
    }

    private FieldErrorDto extractJsonMappingError(JsonMappingException jme, Locale locale) {
        String fieldPath =
                jme.getPath().stream()
                        .map(
                                ref -> {
                                    if (ref.getIndex() >= 0) {
                                        return "[" + ref.getIndex() + "]";
                                    }
                                    return namingResolver.convertFieldName(ref.getFieldName());
                                })
                        .collect(Collectors.joining("."))
                        .replace(".[", "[");

        String messageKey;
        Object[] args = new Object[] {fieldPath};

        if (jme instanceof InvalidFormatException ife) {
            String targetType =
                    ife.getTargetType() != null ? ife.getTargetType().getSimpleName() : "unknown";
            messageKey = "validation.invalidFormat";
            args = new Object[] {fieldPath, targetType};
        } else if (jme instanceof MismatchedInputException) {
            messageKey = "validation.invalidValue";
        } else {
            messageKey = "validation.mappingError";
        }

        String errorMsg = messageSource.getMessage(messageKey, args, locale);
        return new FieldErrorDto(fieldPath, List.of(errorMsg));
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleUnauthorized(
            InternalAuthenticationServiceException ex, Locale locale) {

        String message =
                messageSource.getMessage("error.rest.unauthorized", new Object[] {""}, locale);
        log.error("Unauthorized: {}", ex.getMessage());
        return ResponseBuilder.getError(HttpStatus.UNAUTHORIZED, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleForbidden(
            AccessDeniedException ex, Locale locale) {

        String message =
                messageSource.getMessage("error.rest.forbidden", new Object[] {""}, locale);
        log.error("Access denied: {}", ex.getMessage());
        return ResponseBuilder.getError(HttpStatus.FORBIDDEN, message);
    }

    @ExceptionHandler({MissingPathVariableException.class, NoHandlerFoundException.class})
    public ResponseEntity<ResponseWrapperDto<Object>> handleNotFound(Exception ex, Locale locale) {

        String message =
                messageSource.getMessage("error.rest.not_found", new Object[] {""}, locale);
        log.error("Not found: {}", ex.getMessage());
        return ResponseBuilder.getError(HttpStatus.NOT_FOUND, message);
    }

    @ExceptionHandler({ConversionNotSupportedException.class, Exception.class})
    public ResponseEntity<ResponseWrapperDto<Object>> handleServerError(
            Exception ex, Locale locale) {

        String message = messageSource.getMessage("error.rest.internal_server_error", null, locale);
        log.error("Server error: {}", ex.getMessage(), ex);
        return ResponseBuilder.getError(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseWrapperDto<Object>> handleNoResourceFound(
            NoResourceFoundException ex, Locale locale) {

        String message = messageSource.getMessage("data.notFound", null, locale);
        log.error("No resource found: {}", ex.getMessage());
        return ResponseBuilder.getError(HttpStatus.NOT_FOUND, message);
    }
}
