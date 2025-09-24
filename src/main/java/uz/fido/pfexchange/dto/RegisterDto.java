package uz.fido.pfexchange.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RegisterDto implements Serializable {
    private String username;
    private String password;
    private String confirmPassword;
    @Override
    public String toString() {
        return username;
    }
}
