package uz.fido.pfexchange.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Bekzod",
                        email = "bekzodavazbekugli@gmail.com"
                ),
                title = "Card Processing Service Documentation",
                version = "1.0"
        ),
        security = {
                @SecurityRequirement(
                        name = "BearerAuth"
                )
        }
)
@SecuritySchemes({
        @SecurityScheme(name = "BearerAuth",
                scheme = "bearer",
                type = SecuritySchemeType.HTTP,
                in = SecuritySchemeIn.HEADER,
                bearerFormat = "JWT"),
})
public class OpenApiConfig {
}
