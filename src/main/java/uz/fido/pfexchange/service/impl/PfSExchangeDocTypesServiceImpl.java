package uz.fido.pfexchange.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.entity.PfSExchangeDocTypes;
import uz.fido.pfexchange.repository.PfSExchangeDocTypesRepository;
import uz.fido.pfexchange.service.PfSExchangeDocTypesService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PfSExchangeDocTypesServiceImpl implements PfSExchangeDocTypesService {

    private final PfSExchangeDocTypesRepository pfSExchangeDocTypesRepository;

    @Override
    public String getUrl(String code) {
        return pfSExchangeDocTypesRepository.getPfSExchangeDocTypesByCodeAndIsActiveFlag(code, "Y")
                .orElseThrow(() -> new EntityNotFoundException(code))
                .getPath();
    }

}
