package uz.fido.pfexchange.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uz.fido.pfexchange.annotation.validation.validator.PensTypeValidator;

import java.lang.annotation.*;

@Documented
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PensTypeValidator.class)
public @interface ValidPensType {

    String message() default "{pens.type.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}