package uz.fido.pfexchange.exceptioning;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import uz.fido.pfexchange.utils.ResponseBuilder;

@RestControllerAdvice
@Order(value = Integer.MIN_VALUE)
@RequiredArgsConstructor
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RestExceptionHandler {
    private static final Logger _logger = LogManager.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(value = {
            RestException.class
    })
    public ResponseEntity<?> handleException10(RestException ex) {
        _logger.error(ex);
        return ResponseBuilder.get(ex.getErrorDto(), ex.getStatus());
    }

    @ExceptionHandler(value = {
            MethodArgumentNotValidException.class,
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
            AsyncRequestTimeoutException.class,
    })
    public ResponseEntity<?> handleException20(Exception ex) {
        _logger.error(ex);
        return ResponseBuilder.get(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {
            InternalAuthenticationServiceException.class
    })
    public ResponseEntity<?> handleException30(Exception ex) {
        _logger.error(ex);
        return ResponseBuilder.get("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {
            AccessDeniedException.class
    })
    public ResponseEntity<?> handleException40(Exception ex) {
        _logger.error(ex);
        return ResponseBuilder.get("Access denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {
            MissingPathVariableException.class,
            NoHandlerFoundException.class
    })
    public ResponseEntity<?> handleException50(Exception ex) {
        _logger.error(ex);
        return ResponseBuilder.get(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {
            ConversionNotSupportedException.class,
            Exception.class,
    })
    public ResponseEntity<?> handleException70(Exception ex) {
        _logger.fatal(ex);
        return ResponseBuilder.get(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
