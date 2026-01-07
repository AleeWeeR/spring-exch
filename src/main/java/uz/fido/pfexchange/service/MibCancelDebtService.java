package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.mib.MibCancelDebtPayloadDto;

/**
 * Service interface for MIB debt cancellation API proxy
 * This service only handles the HTTP call to MIB - all business logic is in PL/SQL
 */
public interface MibCancelDebtService {

    /**
     * Send debt cancellation request to MIB pension API
     * This is a simple proxy - the PL/SQL function handles all business logic
     *
     * @param payload The cancellation payload (built by PL/SQL function)
     * @return Response from MIB API as JSON string
     */
    String sendCancelDebtRequest(MibCancelDebtPayloadDto payload);
}
