package uz.fido.pfexchange.config.properties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.logging")
public class ApiLoggingProperties {

    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    private boolean logRequestBody = true;
    @Builder.Default
    private boolean logResponseBody = true;
    @Builder.Default
    private boolean logHeaders = false;
    @Builder.Default
    private int maxBodyLength = 4000;
    @Builder.Default
    private List<String> excludeEndpoints = new ArrayList<>();
    @Builder.Default
    private boolean async = false;
}
