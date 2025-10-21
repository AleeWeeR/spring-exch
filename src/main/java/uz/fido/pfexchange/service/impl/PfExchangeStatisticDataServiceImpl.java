package uz.fido.pfexchange.service.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.PfExchangeStatisticDataDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.entity.OrgPeriodKey;
import uz.fido.pfexchange.entity.Organization;
import uz.fido.pfexchange.entity.PfExchangeStatisticData;
import uz.fido.pfexchange.exceptioning.RestException;
import uz.fido.pfexchange.repository.OrganizationRepository;
import uz.fido.pfexchange.repository.PfExchangeStatisticDataRepository;
import uz.fido.pfexchange.service.PfExchangeStatisticDataService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PfExchangeStatisticDataServiceImpl implements PfExchangeStatisticDataService {

    private final PfExchangeStatisticDataRepository pfExchangeStatisticDataRepository;
    private final OrganizationRepository organizationRepository;
    private final Gson gson;

    @Override
    public PfExchangeStatisticDataDto getStatistics(String period, String coato) throws IOException, SQLException {
        Optional<Organization> organization = organizationRepository.findByCoato(coato);
        if (organization.isPresent()) {
            Optional<PfExchangeStatisticData> data = pfExchangeStatisticDataRepository.findById(OrgPeriodKey.builder()
                    .organizationId(organization.get().getOrganizationId())
                    .period(period)
                    .build());
            if (data.isPresent()) {
                try (InputStream is = data.get().getJson().getBinaryStream();
                     InputStreamReader reader = new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8)) {
                    return gson.fromJson(reader, PfExchangeStatisticDataDto.class);
                }
            } else {
                throw RestException.restThrow(ResponseWrapperDto.builder()
                        .code(Constants.ERROR)
                        .message("So'ralgan davr bo'yicha ma'lumot mavjud emas. (period: %s, coato: %s)".formatted(period, coato))
                        .build());
            }
        } else {
            throw RestException.restThrow(ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("Coato kodi bo'yicha ma'lumot mavjud emas. coato: %s".formatted(coato))
                    .build());
        }
    }
}
