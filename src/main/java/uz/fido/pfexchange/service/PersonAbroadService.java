package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusResponseDto;

/**
 * Pensiya oluvchilar holati uchun servis interfeysi
 * Service interface for pension recipient abroad status
 */
public interface PersonAbroadService {
    /**
     * Pensiya oluvchining holatini tekshirish va kerak bo'lsa faollashtirish
     *
     * @param requestDto So'rov ma'lumotlari (ws_id va pinfl)
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    PersonAbroadStatusResponseDto checkStatus(PersonAbroadStatusRequestDto requestDto);
}
