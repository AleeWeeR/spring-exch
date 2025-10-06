package uz.fido.pfexchange.exceptioning;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.*;
import org.springframework.beans.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindException;
import org.springframework.web.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.utils.ResponseBuilder;


@RestControllerAdvice
@Order(value = Integer.MIN_VALUE)
@RequiredArgsConstructor
public class RestExceptionHandler {
    private static final Logger _logger = LogManager.getLogger(RestExceptionHandler.class);

    // ✅ Custom exception
    @ExceptionHandler(RestException.class)
    public ResponseEntity<?> handleRestException(RestException ex) {
        _logger.error(ex.getMessage(), ex);
        return ResponseBuilder.get(ex.getResponseWrapperDto(), ex.getStatus());
    }

    // ✅ Validation errors (asosiy o‘zgarish shu yerda)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(o -> {
                    _logger.error("-validation_error. field: {}, code: {}, message: {}", o.getField(), o.getCode(), o.getDefaultMessage());

                    if (o.getCode() != null) {
                        return switch (o.getCode()) {
                            case "NotBlank", "NotNull" -> o.getField() + " bo'sh bo'lishi mumkin emas! :(";
                            default -> o.getDefaultMessage();
                        };
                    }
                    return o.getDefaultMessage();
                })
                .orElseGet(() -> {
                    _logger.error("-validation_error. unknown error: {}", ex.getMessage());
                    return "Noma'lum xatolik";
                });

        ResponseWrapperDto response = ResponseWrapperDto.builder()
                .code(Constants.ERROR)
                .message(errorMessage)
                .build();

        return ResponseEntity.ok(response);
    }

    // ✅ Qolgan "bad request" xatoliklari
    @ExceptionHandler({
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestPartException.class,
            ServletRequestBindingException.class,
            MissingServletRequestParameterException.class,
            EmptyResultDataAccessException.class,
            BindException.class,
            DataIntegrityViolationException.class,
            HttpMessageNotWritableException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            HttpMediaTypeNotSupportedException.class,
            AsyncRequestTimeoutException.class
    })
    public ResponseEntity<?> handleBadRequest(Exception ex) {
        _logger.error("Bad request: {}", ex.getMessage(), ex);
        return ResponseBuilder.get(HttpStatus.BAD_REQUEST);
    }

    // ✅ Auth xatoliklari
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<?> handleUnauthorized(Exception ex) {
        _logger.error("Unauthorized: {}", ex.getMessage(), ex);
        return ResponseBuilder.get("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleForbidden(Exception ex) {
        _logger.error("Access denied: {}", ex.getMessage(), ex);
        return ResponseBuilder.get("Access denied", HttpStatus.FORBIDDEN);
    }

    // ✅ Not found
    @ExceptionHandler({
            MissingPathVariableException.class,
            NoHandlerFoundException.class
    })
    public ResponseEntity<?> handleNotFound(Exception ex) {
        _logger.error("Not found: {}", ex.getMessage(), ex);
        return ResponseBuilder.get(HttpStatus.NOT_FOUND);
    }

    // ✅ Umumiy xatolik
    @ExceptionHandler({
            ConversionNotSupportedException.class,
            Exception.class
    })
    public ResponseEntity<?> handleServerError(Exception ex) {
        _logger.fatal("Server error: {}", ex.getMessage(), ex);
        return ResponseBuilder.get(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
