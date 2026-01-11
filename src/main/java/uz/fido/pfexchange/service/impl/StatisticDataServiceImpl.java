package uz.fido.pfexchange.service.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.statistic.StatisticDataDto;
import uz.fido.pfexchange.entity.OrgPeriodKey;
import uz.fido.pfexchange.entity.Organization;
import uz.fido.pfexchange.entity.StatisticData;
import uz.fido.pfexchange.exception.RestException;
import uz.fido.pfexchange.repository.OrganizationRepository;
import uz.fido.pfexchange.repository.StatisticDataRepository;
import uz.fido.pfexchange.service.StatisticDataService;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticDataServiceImpl implements StatisticDataService {

    private final StatisticDataRepository pfExchangeStatisticDataRepository;
    private final OrganizationRepository organizationRepository;
    private final Gson gson;

    @Override
    public StatisticDataDto getStatistics(String period, String coato) {
        Organization organization = findOrganizationByCoato(coato);
        StatisticData data = findStatisticData(organization.getOrganizationId(), period, coato);
        return parseStatisticJson(data, period, coato);
    }

    private Organization findOrganizationByCoato(String coato) {
        return organizationRepository.findByCoato(coato)
            .orElseThrow(() -> RestException.restThrow(
                ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("Coato kodi bo'yicha ma'lumot mavjud emas. coato: %s".formatted(coato))
                    .build()
            ));
    }

    private StatisticData findStatisticData(Integer organizationId, String period, String coato) {
        OrgPeriodKey key = OrgPeriodKey.builder()
            .organizationId(organizationId)
            .period(period)
            .build();

        return pfExchangeStatisticDataRepository.findById(key)
            .orElseThrow(() -> RestException.restThrow(
                ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("So'ralgan davr bo'yicha ma'lumot mavjud emas. (period: %s, coato: %s)"
                        .formatted(period, coato))
                    .build()
            ));
    }

    private StatisticDataDto parseStatisticJson(StatisticData data, String period, String coato) {
        try (InputStream is = data.getJson().getBinaryStream();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            return gson.fromJson(reader, StatisticDataDto.class);

        } catch (SQLException e) {
            log.error("Database error while reading statistic JSON blob - period: {}, coato: {}", 
                period, coato, e);
            throw RestException.restThrow(
                ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("Ma'lumotlar bazasidan o'qishda xatolik yuz berdi. (period: %s, coato: %s)"
                        .formatted(period, coato))
                    .build()
            );
        } catch (IOException e) {
            log.error("IO error while parsing statistic JSON - period: {}, coato: {}", 
                period, coato, e);
            throw RestException.restThrow(
                ResponseWrapperDto.builder()
                    .code(Constants.ERROR)
                    .message("JSON ma'lumotlarini o'qishda xatolik yuz berdi. (period: %s, coato: %s)"
                        .formatted(period, coato))
                    .build()
            );
        }
    }
}