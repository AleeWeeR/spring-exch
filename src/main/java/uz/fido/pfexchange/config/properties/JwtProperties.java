package uz.fido.pfexchange.config.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secretKey;
    private Integer expireHours;
    @Builder.Default private AsymmetricConfig asymmetric = new AsymmetricConfig();

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AsymmetricConfig {
        private boolean enabled;
        private String systemName;
        private String keyId;
        private Integer expireMinutes;
        private String privateKeyPem;
        private String publicKeyPem;
        private String privateKeyBase64;
        private String publicKeyBase64;
    }
}
