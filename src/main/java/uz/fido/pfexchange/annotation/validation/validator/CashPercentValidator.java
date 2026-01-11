package uz.fido.pfexchange.annotation.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uz.fido.pfexchange.annotation.validation.ValidCashPercent;

public class CashPercentValidator implements ConstraintValidator<ValidCashPercent, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        if (value < 10 || value > 100) {
            return false;
        }

        if (value > 90 && value < 100) {
            return false;
        }

        return true;
    }
}