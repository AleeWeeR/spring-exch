package uz.fido.pfexchange.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "Pension Integration Service",
                        description =
                                "Handles integration between the pension system and external"
                                        + " government or third-party services.",
                        version = "1.0"),
        security = {@SecurityRequirement(name = "BearerAuth")})
@SecuritySchemes({
    @SecurityScheme(
            name = "BearerAuth",
            scheme = "bearer",
            type = SecuritySchemeType.HTTP,
            in = SecuritySchemeIn.HEADER,
            bearerFormat = "JWT"),
    @SecurityScheme(
            name = "BasicAuth",
            scheme = "basic",
            type = SecuritySchemeType.HTTP,
            description = "Basic Authentication for login endpoint")
})
public class OpenApiConfig {}
