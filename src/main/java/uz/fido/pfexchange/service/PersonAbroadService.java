package uz.fido.pfexchange.service;

import uz.fido.pfexchange.dto.mip.PersonAbroadCheckStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadRestoreStatusResponseDto;
import uz.fido.pfexchange.dto.mip.PersonAbroadStatusRequestDto;

/**
 * Pensiya oluvchilar holati uchun servis interfeysi
 * Service interface for pension recipient abroad status
 */
public interface PersonAbroadService {
    /**
     * Pensiya oluvchining holatini FAQAT tekshirish (faollashtirishsiz)
     * Just check person status without restoration
     *
     * Response:
     *   result: 1=success, 0=error
     *   status: 1=faol, 2=nofaol (chet elda), 3=nofaol (boshqa sabablar)
     *
     * @param requestDto So'rov ma'lumotlari (ws_id va pinfl)
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    PersonAbroadCheckStatusResponseDto checkStatus(PersonAbroadStatusRequestDto requestDto);

    /**
     * Pensiya oluvchini tiklash (close_desc=11 bo'lganlar uchun)
     * Restore person status (for those with close_desc=11)
     *
     * Response result codes:
     *   0 = Pensiya oluvchilar ro'yhatida mavjud emas
     *   1 = Pensiya oluvchilar ro'yhatida mavjud
     *   2 = Oluvchi statusi faol xolatga keltirildi
     *   3 = O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
     *
     * @param requestDto So'rov ma'lumotlari (ws_id va pinfl)
     * @return Holat kodi va ma'lumotlar bilan javob
     */
    PersonAbroadRestoreStatusResponseDto restoreStatus(PersonAbroadStatusRequestDto requestDto);
}
