package uz.fido.pfexchange.service;

import jakarta.validation.constraints.NotNull;
import uz.fido.pfexchange.dto.statistic.StatisticDataDto;

public interface StatisticDataService {

    StatisticDataDto getStatistics(@NotNull String period, @NotNull String coato);
}
