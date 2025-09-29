package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.*;
import uz.fido.pfexchange.entity.User;
import uz.fido.pfexchange.exceptioning.RestException;
import uz.fido.pfexchange.security.JwtService;
import uz.fido.pfexchange.service.AuthService;
import uz.fido.pfexchange.service.UserService;

import java.util.Collections;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public TokenDto login(@NonNull LoginDto login) throws UsernameNotFoundException {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            login.getUsername(),
                            login.getPassword()
                    )
            );
        } catch (CredentialsExpiredException e) {
            throw RestException.restThrow(ErrorDto.builder()
                    .code(Constants.Error.FORBIDDEN)
                    .message("Credentials expired!")
                    .build(), HttpStatus.FORBIDDEN);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw RestException.restThrow(ErrorDto.builder()
                    .code(Constants.Error.UNAUTHORIZED)
                    .message("Login or password incorrect!")
                    .build(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        UserDetails userDetails = userService.loadUserByUsername(login.getUsername());
        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .build();
    }

    @Override
    public void register(@NonNull RegisterDto register) throws RestException {
        if (Objects.isNull(register.getPassword()) || !Objects.equals(register.getPassword(), register.getConfirmPassword()))
            throw RestException.restThrow(ErrorDto.builder()
                    .code(Constants.Error.PRECONDITION_FAILED)
                    .message("Passwords not matched or empty!")
                    .build(), HttpStatus.PRECONDITION_FAILED);
        var user = User.builder()
                .username(register.getUsername())
                .password(passwordEncoder.encode(register.getPassword()))
                .name(register.getUsername())
                .build();
        userService.save(user);
    }
}
