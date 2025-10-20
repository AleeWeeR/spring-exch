package uz.fido.pfexchange.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;

public class ResponseBuilder {
    public static <T> ResponseEntity<ResponseWrapperDto<T>> ok(T data) {
        return get(data, HttpStatus.OK, Constants.SUCCESS, null);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> getSuccess(T data, HttpStatus status) {
        return get(data, status, Constants.SUCCESS, null);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> getError(T data, HttpStatus status) {
        return get(data, status, Constants.ERROR, null);
    }

    public static ResponseEntity<ResponseWrapperDto<Object>> getSuccess(HttpStatus status, String message) {
        return get(null, status, Constants.SUCCESS, message);
    }

    public static ResponseEntity<ResponseWrapperDto<Object>> getError(HttpStatus status, String message) {
        return get(null, status, Constants.ERROR, message);
    }

    public static <T> ResponseEntity<ResponseWrapperDto<T>> get(T data, HttpStatus status, int code, String message) {
        ResponseWrapperDto<T> responseWrapperDto = new ResponseWrapperDto<>();
        responseWrapperDto.setData(data);
        responseWrapperDto.setMessage(message);
        responseWrapperDto.setCode(code);

        return new ResponseEntity<>(responseWrapperDto, status);
    }

}
