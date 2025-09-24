package uz.fido.pfexchange.service;

import org.springframework.lang.NonNull;
import uz.fido.pfexchange.dto.ActivitiesPostDto;
import uz.fido.pfexchange.dto.LoginDto;
import uz.fido.pfexchange.dto.RegisterDto;
import uz.fido.pfexchange.dto.TokenDto;

public interface AuthService {
    TokenDto login(@NonNull LoginDto login);

    void register(@NonNull RegisterDto register);
}
