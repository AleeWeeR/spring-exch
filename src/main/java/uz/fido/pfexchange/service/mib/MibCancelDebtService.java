package uz.fido.pfexchange.service.mib;

import uz.fido.pfexchange.dto.mib.MibCancelDebtRequestDto;

/**
 * Service interface for debt cancellation operations
 * Handles cancellation of debts with MIB pension system
 */
public interface MibCancelDebtService {

    /**
     * Send debt cancellation request to MIB pension API
     * This is a simple proxy - the PL/SQL function handles all business logic
     *
     * @param payload The cancellation payload (built by PL/SQL function)
     * @return Response from MIB API as JSON string
     */
     String sendCancelDebtRequest(MibCancelDebtRequestDto payload);
}