package uz.fido.pfexchange.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ConversionException extends RuntimeException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
