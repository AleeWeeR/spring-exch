package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusResponseDto;

/**
 * Pensiya oluvchilar holati uchun servis interfeysi
 * Service interface for pension recipient abroad status
 */
public interface PersonAbroadService {
    /**
     * Pensiya oluvchining holatini FAQAT tekshirish (faollashtirishsiz)
     * Just check person status without restoration
     *
     * Natija kodlari:
     * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
     * 1 - Faol (active)
     * 2 - Nofaol, close_desc=11 (chet elda 3 oydan ortiq)
     * 3 - Nofaol, boshqa sabablar bilan
     *
     * @param requestDto So'rov ma'lumotlari (ws_id va pinfl)
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    PersonAbroadStatusResponseDto checkStatus(PersonAbroadStatusRequestDto requestDto);

    /**
     * Pensiya oluvchini tiklash (close_desc=11 bo'lganlar uchun)
     * Restore person status (for those with close_desc=11)
     *
     * Natija kodlari:
     * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
     * 1 - Pensiya oluvchilar ro'yhatida mavjud (already active)
     * 2 - Oluvchi statusi faol xolatga keltirildi
     * 3 - O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
     *
     * @param requestDto So'rov ma'lumotlari (ws_id va pinfl)
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    PersonAbroadStatusResponseDto restoreStatus(PersonAbroadStatusRequestDto requestDto);
}
