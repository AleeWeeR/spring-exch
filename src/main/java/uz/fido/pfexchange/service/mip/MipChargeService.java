package uz.fido.pfexchange.service.mip;


import uz.fido.pfexchange.dto.mip.charge.MipChargeHistoryResponseDto;
import uz.fido.pfexchange.dto.mip.charge.MipChargeRequestDto;
import uz.fido.pfexchange.dto.mip.charge.MipChargeResponseDto;

/**
 * Service interface for charge operations
 */
public interface MipChargeService {

    /**
     * Get current charge information for a person
     * Calls PF_EXCHANGES_EP_CHARGE.Get_Charges_Info function
     *
     * @param request Request containing ws_id and pinfl
     * @return Charge information with debt details
     */
    MipChargeResponseDto getChargesInfo(MipChargeRequestDto request);

    /**
     * Get charged history information for a person
     * Calls PF_EXCHANGES_EP_CHARGE.Get_Charged_Info function
     *
     * @param request Request containing ws_id and pinfl
     * @return Charged history with period details
     */
    MipChargeHistoryResponseDto getChargedInfo(MipChargeRequestDto request);
}