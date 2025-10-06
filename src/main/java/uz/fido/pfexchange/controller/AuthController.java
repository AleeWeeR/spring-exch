package uz.fido.pfexchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.*;
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
    public ResponseEntity<ResponseWrapperDto> login(@Valid @RequestBody LoginDto login) {
        TokenDto token = authService.login(login);
        _logger.info("Login success. user -> {}", login);

        return ResponseEntity.ok(
                ResponseWrapperDto
                        .builder()
                        .code(Constants.SUCCESS)
                        .data(token)
                        .build()
        );
    }

    @PostMapping(value = "register",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ResponseWrapperDto> register(@Valid @RequestBody RegisterDto register) {
        authService.register(register);
        _logger.info("Register success. user -> {}", register);
        return ResponseEntity.ok(
                ResponseWrapperDto
                        .builder()
                        .code(Constants.SUCCESS)
                        .message("Ma'lumotlar muvaffaqqiyatli saqlandi!")
                        .build()
        );
    }
}