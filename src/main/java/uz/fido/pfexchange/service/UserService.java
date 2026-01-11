package uz.fido.pfexchange.service;

import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;
import uz.fido.pfexchange.dto.RegisterDto;
import uz.fido.pfexchange.dto.user.UserDto;

public interface UserService extends UserDetailsService {
    UserDto getById(Long id);

    List<UserDto> getAll();

    void register(RegisterDto register);

    UserDto save(UserDto user);

    boolean existsByUsername(String username);
}
