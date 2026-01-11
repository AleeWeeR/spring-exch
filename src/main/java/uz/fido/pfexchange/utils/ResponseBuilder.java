package uz.fido.pfexchange.utils;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.FieldErrorDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;

public class ResponseBuilder {
    public static <T> ResponseEntity<ResponseWrapperDto<T>> ok(T data) {
        return get(data, HttpStatus.OK, Constants.SUCCESS, null, null);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> getSuccess(T data, HttpStatus status) {
        return get(data, status, Constants.SUCCESS, null, null);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> getError(T data, HttpStatus status) {
        return get(data, status, Constants.ERROR, null, null);
    }

    public static ResponseEntity<ResponseWrapperDto<Object>> getSuccess(
            HttpStatus status, String message) {
        return get(null, status, Constants.SUCCESS, message, null);
    }

    public static ResponseEntity<ResponseWrapperDto<Object>> getError(
            HttpStatus status, String message) {
        return get(null, status, Constants.ERROR, message, null);
    }
    
    public static ResponseEntity<ResponseWrapperDto<Object>> getError(
            HttpStatus status, String message, List<FieldErrorDto> errors) {
        return get(null, status, Constants.ERROR, message, errors);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> getError(
            T data, HttpStatus status, int code, String message) {
        return get(data, status, code, message, null);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> get(
            T data, HttpStatus status, int code, String message, List<FieldErrorDto> errors) {
        ResponseWrapperDto<T> responseWrapperDto = new ResponseWrapperDto<>();
        responseWrapperDto.setData(data);
        responseWrapperDto.setMessage(message);
        responseWrapperDto.setCode(code);
        responseWrapperDto.setErrors(errors);

        return new ResponseEntity<>(responseWrapperDto, status);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResponseWrapperDto.<T>builder().code(0).message(message).data(null).build());
    }
}
