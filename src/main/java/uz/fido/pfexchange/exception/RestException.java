package uz.fido.pfexchange.exception;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpStatus;
import uz.fido.pfexchange.dto.ResponseWrapperDto;

import java.util.function.Supplier;

@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = true)
public class RestException extends RuntimeException implements Supplier<RuntimeException> {

    private ResponseWrapperDto<?> responseWrapperDto;
    private HttpStatus status;

    public <T> RestException(ResponseWrapperDto<T> responseWrapperDto, HttpStatus status) {
        super(responseWrapperDto.getMessage());
        this.responseWrapperDto = responseWrapperDto;
        this.status = status;
    }

    public static <T> RestException restThrow(ResponseWrapperDto<T> responseWrapperDto, HttpStatus status) {
        return new RestException(responseWrapperDto, status);
    }

    public static <T> RestException restThrow(ResponseWrapperDto<T> responseWrapperDto) {
        return new RestException(responseWrapperDto, HttpStatus.BAD_REQUEST);
    }

    @Override
    public RuntimeException get() {
        return this;
    }
}
