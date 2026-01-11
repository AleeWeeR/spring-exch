package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uz.fido.pfexchange.config.properties.JwtProperties;
import uz.fido.pfexchange.dto.*;
import uz.fido.pfexchange.security.JwtService;
import uz.fido.pfexchange.service.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final UserService userService;
    private final JwtProperties properties;

    @Override
    @Transactional
    public TokenDto login(@NonNull String username) {
        UserDetails userDetails = userService.loadUserByUsername(username);

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return TokenDto.builder()
                .tokenType("Bearer")
                .expiresIn(properties.getExpireHours() * 60 * 60)
                .accessToken(newAccessToken)
                .build();
    }
}
