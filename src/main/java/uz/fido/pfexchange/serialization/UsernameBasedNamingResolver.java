package uz.fido.pfexchange.serialization;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import uz.fido.pfexchange.config.properties.SerializationProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class UsernameBasedNamingResolver implements NamingStrategyResolver {

    private final SerializationProperties properties;

    public UsernameBasedNamingResolver(SerializationProperties properties) {
        this.properties = properties;
    }

    @Override
    public PropertyNamingStrategy resolve() {
        if (NamingStrategyOverrideHolder.hasOverride()) {
            return NamingStrategyOverrideHolder.get().toJacksonStrategy();
        }

        String username = extractUsername();
        return properties.resolveByUsername(username).toJacksonStrategy();
    }

    @Override
    public String convertFieldName(String fieldName) {
        PropertyNamingStrategy strategy = resolve();

        if (strategy instanceof PropertyNamingStrategies.SnakeCaseStrategy) {
            return Arrays.stream(fieldName.split("\\."))
                    .map(this::toSnakeCase)
                    .collect(Collectors.joining("."));
        }
        return fieldName;
    }

    private String extractUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return null;
    }

    private String toSnakeCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i > 0) result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }
}
