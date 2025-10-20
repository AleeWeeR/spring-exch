package uz.fido.pfexchange.service;

import jakarta.validation.constraints.NotNull;
import uz.fido.pfexchange.dto.PfExchangeStatisticDataDto;

import java.io.IOException;
import java.sql.SQLException;

public interface PfExchangeStatisticDataService {

    PfExchangeStatisticDataDto getStatistics(@NotNull String period, @NotNull String coato) throws IOException, SQLException;
}
