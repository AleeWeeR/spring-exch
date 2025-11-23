package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Restore status response DTO
 * Response for /restore-status endpoint
 *
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
@Schema(description = "Restore status javobi")
public class PersonAbroadRestoreStatusResponseDto {

    @Schema(
            description = "Natija kodi: 0=topilmadi, 1=mavjud, 2=tiklandi, 3=kirganlik aniqlanmadi",
            example = "2",
            required = true
    )
    @JsonProperty("result")
    private Integer result;

    @Schema(
            description = "Xabar matni",
            example = "O'zgartirildi",
            required = true
    )
    @JsonProperty("msg")
    private String msg;

    @Schema(
            description = "Veb-servis identifikatori",
            example = "77",
            required = true
    )
    @JsonProperty("ws_id")
    private Long wsId;
}
