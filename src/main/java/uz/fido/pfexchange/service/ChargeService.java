package uz.fido.pfexchange.service;


import uz.fido.pfexchange.dto.mip.ChargeHistoryResponseDto;
import uz.fido.pfexchange.dto.mip.ChargeRequestDto;
import uz.fido.pfexchange.dto.mip.ChargeResponseDto;

/**
 * Service interface for charge operations
 */
public interface ChargeService {

    /**
     * Get current charge information for a person
     * Calls PF_EXCHANGES_EP_CHARGE.Get_Charges_Info function
     *
     * @param request Request containing ws_id and pinfl
     * @return Charge information with debt details
     */
    ChargeResponseDto getChargesInfo(ChargeRequestDto request);

    /**
     * Get charged history information for a person
     * Calls PF_EXCHANGES_EP_CHARGE.Get_Charged_Info function
     *
     * @param request Request containing ws_id and pinfl
     * @return Charged history with period details
     */
    ChargeHistoryResponseDto getChargedInfo(ChargeRequestDto request);
}