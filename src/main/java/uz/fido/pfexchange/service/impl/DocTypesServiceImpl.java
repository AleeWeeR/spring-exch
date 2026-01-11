package uz.fido.pfexchange.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.repository.DocTypesRepository;
import uz.fido.pfexchange.service.DocTypesService;

@Service
@RequiredArgsConstructor
public class DocTypesServiceImpl
    implements DocTypesService {

    private final DocTypesRepository docTypesRepository;

    @Override
    public String getUrl(String code) {
        return docTypesRepository
            .getDocTypesByCodeAndIsActiveFlag(code, "Y")
            .orElseThrow(() -> new EntityNotFoundException(code))
            .getPath();
    }
}
