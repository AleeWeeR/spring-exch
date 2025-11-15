package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WS ID status javobi uchun DTO
 * Response DTO for WS ID status query
 *
 * Natija kodlari:
 * 0 - Pensiya oluvchilar ro'yhatida mavjud emas
 * 1 - Pensiya oluvchilar ro'yhatida mavjud
 * 2 - Oluvchi statusi faol xolatga keltirildi
 * 3 - O'zbekiston Respublikasi hududiga kirganlik holati aniqlanmadi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Pensiya oluvchi holatini tekshirish javobi")
public class WsIdStatusResponseDto {

    @Schema(
            description = "Natija kodi: 0=Ro'yhatda yo'q, 1=Ro'yhatda mavjud, 2=Faol xolatga keltirildi, 3=Kirganlik aniqlanmadi",
            example = "1",
            required = true
    )
    @JsonProperty("result")
    private Integer result;

    @Schema(
            description = "Xabar matni",
            example = "O'zgartirildi"
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

    @Schema(
            description = "Oluvchi holati (1=faol, 0=nofaol)",
            example = "1"
    )
    @JsonProperty("status")
    private Integer status;
}
