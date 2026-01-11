package uz.fido.pfexchange.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RegisterDto implements Serializable {
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 8, message = "Parol kamida 8 ta belgidan iborat bo‘lishi kerak!")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$",
            message = "password kamida 1ta katta harf, 1ta kichik harf, 1ta raqam va 1ta maxsus belgi (@$!%*?&) dan iborat bo‘lishi kerak!"
    )
    private String password;

    @NotBlank
    private String confirmPassword;
    
    private String addInfo;

    @Override
    public String toString() {
        return username;
    }
}
