package uz.fido.pfexchange.annotation.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.RequiredArgsConstructor;

import uz.fido.pfexchange.annotation.validation.ValidPensType;
import uz.fido.pfexchange.repository.CustomQueryRepository;

@RequiredArgsConstructor
public class PensTypeValidator implements ConstraintValidator<ValidPensType, String> {

    private final CustomQueryRepository customQueryRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return customQueryRepository.pensTypeExists(value);
    }
}
