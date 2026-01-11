package uz.fido.pfexchange.annotation;

import uz.fido.pfexchange.config.properties.SerializationProperties.NamingType;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NamingStrategy {
    NamingType value();
}