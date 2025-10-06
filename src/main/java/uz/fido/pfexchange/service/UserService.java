package uz.fido.pfexchange.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import uz.fido.pfexchange.entity.User;

public interface UserService extends UserDetailsService {
    /**
     * Foydalanuvchini saqlash
     */
    User save(User user);

    /**
     * Username bazada mavjudligini tekshirish (faol foydalanuvchilar)
     */
    boolean existsByUsername(String username);
}
