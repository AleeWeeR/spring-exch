package uz.fido.pfexchange.annotation.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import uz.fido.pfexchange.annotation.NamingStrategy;
import uz.fido.pfexchange.serialization.NamingStrategyOverrideHolder;

@Component
public class NamingStrategyInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            NamingStrategy annotation = handlerMethod.getMethodAnnotation(NamingStrategy.class);

            if (annotation == null) {
                annotation = handlerMethod.getBeanType().getAnnotation(NamingStrategy.class);
            }

            if (annotation != null) {
                NamingStrategyOverrideHolder.set(annotation.value());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {
        NamingStrategyOverrideHolder.clear();
    }
}
