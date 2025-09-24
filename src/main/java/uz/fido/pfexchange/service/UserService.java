package uz.fido.pfexchange.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import uz.fido.pfexchange.entity.User;

public interface UserService extends UserDetailsService {
    User save(User user);
}
