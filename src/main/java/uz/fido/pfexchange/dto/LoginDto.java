package uz.fido.pfexchange.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JacksonXmlRootElement(localName = "data")
public class LoginDto implements Serializable {
    private String username;
    private String password;

    @Override
    public String toString() {
        return username;
    }
}
