package uz.fido.pfexchange.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uz.fido.pfexchange.annotation.validation.validator.CashPercentValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CashPercentValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCashPercent {

    String message() default "{validation.cashPercent.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}