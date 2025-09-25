package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.LoginDto;
import uz.fido.pfexchange.dto.RegisterDto;
import uz.fido.pfexchange.dto.TokenDto;
import uz.fido.pfexchange.service.AuthService;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger _logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping(value = "login",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<TokenDto> login(@RequestBody LoginDto login) {
        TokenDto token = authService.login(login);
        _logger.info("Login success. user -> %s".formatted(login));
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @PostMapping(value = "register",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<?> register(@RequestBody RegisterDto register) {
        authService.register(register);
        _logger.info("Register success. user -> %s".formatted(register));
        return ResponseEntity.ok("Ma'lumot saqlandi!");
    }

}