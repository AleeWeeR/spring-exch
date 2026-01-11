package uz.fido.pfexchange.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uz.fido.pfexchange.annotation.interceptor.NamingStrategyInterceptor;
import uz.fido.pfexchange.serialization.DynamicNamingMessageConverter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DynamicNamingMessageConverter dynamicConverter;
    private final NamingStrategyInterceptor namingStrategyInterceptor;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, dynamicConverter);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(namingStrategyInterceptor);
    }
}
