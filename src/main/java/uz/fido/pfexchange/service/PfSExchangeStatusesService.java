package uz.fido.pfexchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.entity.PfSExchangeStatus;
import uz.fido.pfexchange.repository.PfSExchangeStatusRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PfSExchangeStatusesService {

    private final PfSExchangeStatusRepository pfSExchangeStatusesRepository;

    public Optional<PfSExchangeStatus> getByCode(@NonNull String code) {
        return pfSExchangeStatusesRepository.findByCode(code);
    }

    public List<PfSExchangeStatus> getAll() {
        return pfSExchangeStatusesRepository.findAllBy();
    }

}