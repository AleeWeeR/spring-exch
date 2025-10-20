package uz.fido.pfexchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.*;
import uz.fido.pfexchange.service.AuthService;
import uz.fido.pfexchange.utils.ResponseBuilder;

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
    public ResponseEntity<ResponseWrapperDto<TokenDto>> login(@Valid @RequestBody LoginDto login) {
        TokenDto token = authService.login(login);
        _logger.info("Login success. user -> {}", login);

        return ResponseBuilder.ok(token);
    }

    @PostMapping(value = "register",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ResponseWrapperDto<Object>> register(@Valid @RequestBody RegisterDto register) {
        authService.register(register);
        _logger.info("Register success. user -> {}", register);
        return ResponseBuilder.getError(HttpStatus.OK, "Ma'lumotlar muvaffaqqiyatli saqlandi!");
    }
}