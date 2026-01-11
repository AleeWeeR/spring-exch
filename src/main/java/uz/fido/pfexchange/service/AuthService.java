package uz.fido.pfexchange.service;

import org.springframework.lang.NonNull;
import uz.fido.pfexchange.dto.TokenDto;

public interface AuthService {
    TokenDto login(@NonNull String username);
}
