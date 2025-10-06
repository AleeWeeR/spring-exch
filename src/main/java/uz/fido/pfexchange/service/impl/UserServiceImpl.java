package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.entity.User;
import uz.fido.pfexchange.repository.UserRepository;
import uz.fido.pfexchange.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Foydalanuvchini username bo‘yicha yuklash
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameAndIsActiveFlag(username, "Y")
                .orElseThrow(() -> new UsernameNotFoundException("Foydalanuvchi topilmadi"));
    }

    /**
     * Foydalanuvchini saqlash
     */
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    /**
     * Username bazada mavjudligini tekshirish
     */
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsernameAndIsActiveFlag(username, "Y").isPresent();
    }
}
