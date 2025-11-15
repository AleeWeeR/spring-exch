package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.mip.WsIdStatusRequestDto;
import uz.fido.pfexchange.dto.mip.WsIdStatusResponseDto;

/**
 * Pensiya oluvchilar holati uchun servis interfeysi
 * Service interface for pension recipient status
 */
public interface WsIdStatusService {
    /**
     * Pensiya oluvchining holatini tekshirish va kerak bo'lsa faollashtirish
     *
     * @param requestDto So'rov ma'lumotlari (ws_id va pinfl)
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    WsIdStatusResponseDto checkStatus(WsIdStatusRequestDto requestDto);
}
