package uz.fido.pfexchange.service.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.PfExchangeStatisticDataDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.exceptioning.RestException;
import uz.fido.pfexchange.repository.PfExchangeStatisticDataRepository;
import uz.fido.pfexchange.service.PfExchangeStatisticDataService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class PfExchangeStatisticDataServiceImpl implements PfExchangeStatisticDataService {

    private final PfExchangeStatisticDataRepository pfExchangeStatisticDataRepository;
    private final Gson gson;

    @Override
    public PfExchangeStatisticDataDto getStatistics(String period, String coato) throws IOException, SQLException {
        Blob json = pfExchangeStatisticDataRepository.findByOrganization_CoatoAndId_Period(coato, period)
                .orElseThrow(() -> RestException.restThrow(ResponseWrapperDto.builder()
                        .code(Constants.ERROR)
                        .message("So'ralgan davr bo'yicha ma'lumot mavjud emas")
                        .build()))
                .getJson();

        try (InputStream is = json.getBinaryStream();
             InputStreamReader reader = new InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, PfExchangeStatisticDataDto.class);
        }
    }
}
