package uz.fido.pfexchange.exceptioning;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import uz.fido.pfexchange.dto.ResponseWrapperDto;

import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestException extends RuntimeException implements Supplier<RuntimeException> {

    private ResponseWrapperDto responseWrapperDto;
    private HttpStatus status;

    public RestException(ResponseWrapperDto responseWrapperDto, HttpStatus status) {
        super(responseWrapperDto.getMessage());
        this.responseWrapperDto = responseWrapperDto;
        this.status = status;
    }

    private RestException(HttpStatus status) {
        this.status = status;
    }

    public static RestException restThrow(ResponseWrapperDto responseWrapperDto, HttpStatus status) {
        return new RestException(responseWrapperDto, status);
    }

    public static RestException restThrow(ResponseWrapperDto responseWrapperDto) {
        return new RestException(responseWrapperDto, HttpStatus.BAD_REQUEST);
    }

    public static RestException restThrow(HttpStatus status) {
        return new RestException(status);
    }

    @Override
    public RuntimeException get() {
        return this;
    }
}
