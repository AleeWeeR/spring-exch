package uz.fido.pfexchange.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import jakarta.validation.constraints.NotNull;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "app.serialization")
public class SerializationProperties {

    @NotNull
    private NamingType defaultStrategy = NamingType.CAMEL;

    private Map<NamingType, StrategyMatcher> strategies = new EnumMap<>(NamingType.class);
    
    public NamingType resolveByUsername(String username) {
        if (username == null) {
            return defaultStrategy;
        }
        
        return strategies.entrySet().stream()
            .filter(e -> e.getValue().matchesUsername(username))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(defaultStrategy);
    }

    public enum NamingType {
        SNAKE(PropertyNamingStrategies.SNAKE_CASE),
        CAMEL(PropertyNamingStrategies.LOWER_CAMEL_CASE);

        private final PropertyNamingStrategy strategy;

        NamingType(PropertyNamingStrategy strategy) {
            this.strategy = strategy;
        }

        public PropertyNamingStrategy toJacksonStrategy() {
            return strategy;
        }
    }

    @Getter
    @Setter
    public static class StrategyMatcher {
        
        private Set<String> username = new HashSet<>();

        public boolean matchesUsername(String name) {
            return username.contains(name);
        }
    }
}
