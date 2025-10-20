package uz.fido.pfexchange.exceptioning;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import uz.fido.pfexchange.dto.ResponseWrapperDto;

import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Data
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
