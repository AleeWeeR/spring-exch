package uz.fido.pfexchange.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import uz.fido.pfexchange.logging.OutboundLoggingInterceptor;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final OutboundLoggingInterceptor loggingInterceptor;

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(45_000);

        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(factory))
                .requestInterceptor(loggingInterceptor)
                .build();
    }
}
