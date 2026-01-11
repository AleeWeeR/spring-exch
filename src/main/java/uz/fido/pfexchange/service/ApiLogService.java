package uz.fido.pfexchange.service;

import uz.fido.pfexchange.entity.CoreExchangesLog;

public interface ApiLogService {

    void log(CoreExchangesLog logEntry);

    void logSync(CoreExchangesLog logEntry);

    void logAsync(CoreExchangesLog logEntry);
}
