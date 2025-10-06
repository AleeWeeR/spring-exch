package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.*;
import uz.fido.pfexchange.entity.User;
import uz.fido.pfexchange.exceptioning.RestException;
import uz.fido.pfexchange.security.JwtService;
import uz.fido.pfexchange.service.*;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenDto login(@NonNull LoginDto login) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            login.getUsername(),
                            login.getPassword()
                    )
            );
        } catch (CredentialsExpiredException e) {
            throw RestException.restThrow(
                    ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message("Parol muddati tugagan!")
                            .build(),
                    HttpStatus.FORBIDDEN
            );
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw RestException.restThrow(
                    ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message("Login yoki parol noto‘g‘ri!")
                            .build(),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            throw RestException.restThrow(
                    ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message("Kutilmagan xatolik: " + e.getMessage())
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        UserDetails userDetails = userService.loadUserByUsername(login.getUsername());
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    @Transactional
    public void register(@NonNull RegisterDto register) {
        // Parollar mosligini tekshirish
        if (!Objects.equals(register.getPassword(), register.getConfirmPassword())) {
            throw RestException.restThrow(
                    ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message("Parollar mos emas!")
                            .build(),
                    HttpStatus.PRECONDITION_FAILED
            );
        }

        // Foydalanuvchi nomining yagona ekanligini tekshirish
        if (userService.existsByUsername(register.getUsername().trim())) {
            throw RestException.restThrow(
                    ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message("Foydalanuvchi nomi allaqachon mavjud!")
                            .build(),
                    HttpStatus.CONFLICT
            );
        }

        // User yaratish
        var user = User.builder()
                .username(register.getUsername().trim())
                .password(passwordEncoder.encode(register.getPassword()))
                .name(register.getUsername().trim())
                .isActiveFlag("Y")
                .build();

        userService.save(user);
    }

}
