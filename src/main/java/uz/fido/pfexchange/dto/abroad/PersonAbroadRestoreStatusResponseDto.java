package uz.fido.pfexchange.dto.abroad;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Restore status response DTO
 * Response for /restore-status endpoint
 * result codes:
 *   0 = Pensiya oluvchilar ro'yhatida mavjud emas
 *   1 = Pensiya oluvchilar ro'yhatida mavjud
 *   2 = Oluvchi statusi faol xolatga keltirildi
 *   3 = O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonAbroadRestoreStatusResponseDto {


    @JsonProperty("result")
    private Integer result;


    @JsonProperty("msg")
    private String msg;


    @JsonProperty("ws_id")
    private Long wsId;
}
