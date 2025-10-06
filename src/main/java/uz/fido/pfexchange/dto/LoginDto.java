package uz.fido.pfexchange.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JacksonXmlRootElement(localName = "data")
public class LoginDto implements Serializable {
    @NotBlank(message = "username bo‘sh bo‘lishi mumkin emas!")
    private String username;

    @NotBlank(message = "password bo‘sh bo‘lishi mumkin emas!")
    private String password;

    @Override
    public String toString() {
        return username;
    }
}
