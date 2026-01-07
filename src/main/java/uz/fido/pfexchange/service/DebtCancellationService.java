package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.mib.DebtCancellationRequestDto;
import uz.fido.pfexchange.dto.mib.DebtCancellationResponseDto;

/**
 * Service interface for debt cancellation operations
 * Handles cancellation of debts with MIB pension system
 */
public interface DebtCancellationService {

    /**
     * Cancel debt for a specific external ID
     * This method:
     * 1. Queries the database for debt information
     * 2. Calls MIB pension API to cancel the debt
     * 3. Updates database records with the result
     *
     * @param requestDto Request containing external_id
     * @return Response with cancellation result
     */
    DebtCancellationResponseDto cancelDebt(DebtCancellationRequestDto requestDto);

    /**
     * Automatically check and cancel debts for a person when they are closed
     * This is triggered when a person or application is closed
     *
     * @param pinpp Person's PINFL
     * @param closeReason Close reason code (from person or application)
     */
    void autoCheckAndCancelDebts(String pinpp, String closeReason);
}
