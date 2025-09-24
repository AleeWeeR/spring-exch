package uz.fido.pfexchange.exceptioning;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import uz.fido.pfexchange.dto.ErrorDto;

import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestException extends RuntimeException implements Supplier<RuntimeException> {

    private ErrorDto errorDto;
    private HttpStatus status;

    public RestException(ErrorDto errorDto, HttpStatus status) {
        super(errorDto.getMessage());
        this.errorDto = errorDto;
        this.status = status;
    }

    private RestException(HttpStatus status) {
        this.status = status;
    }

    public static RestException restThrow(ErrorDto errorDto, HttpStatus status) {
        return new RestException(errorDto, status);
    }

    public static RestException restThrow(ErrorDto errorDto) {
        return new RestException(errorDto, HttpStatus.BAD_REQUEST);
    }

    public static RestException restThrow(HttpStatus status) {
        return new RestException(status);
    }

    @Override
    public RuntimeException get() {
        return this;
    }
}
