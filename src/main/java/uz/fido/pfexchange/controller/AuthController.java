package uz.fido.pfexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import uz.fido.pfexchange.dto.*;
import uz.fido.pfexchange.service.AuthService;
import uz.fido.pfexchange.utils.ResponseBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("login")
    @Operation(
            summary = "Login with username and password",
            description = "Authenticate using Basic Auth and receive JWT token",
            security = @SecurityRequirement(name = "BasicAuth"))
    public ResponseEntity<ResponseWrapperDto<TokenDto>> login(Authentication authentication) {
        TokenDto token = authService.login(authentication.getName());

        return ResponseBuilder.ok(token);
    }

    @PostMapping("mip/login")
    @Operation(
            summary = "Login with username and password",
            description = "Authenticate using Basic Auth and receive JWT token",
            security = @SecurityRequirement(name = "BasicAuth"))
    public ResponseEntity<TokenDto> mipLogin(Authentication authentication) {
        TokenDto token = authService.login(authentication.getName());

        return ResponseEntity.ok(token);
    }
}
