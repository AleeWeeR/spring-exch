package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WS ID status ma'lumotlari uchun DTO
 * Data DTO for WS ID status query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "WS ID va PINFL ma'lumotlari")
public class WsIdStatusDataDto {

    @Schema(
            description = "Veb-servis identifikatori (musbat butun son)",
            example = "77",
            required = true
    )
    @NotNull(message = "ws_id - majburiy parametr (bo'sh bo'lmasligi kerak)")
    @Positive(message = "ws_id - musbat son bo'lishi kerak")
    @JsonProperty("ws_id")
    private Long wsId;

    @Schema(
            description = "Shaxsiy identifikatsiya raqami (pinfl) - 14 xonali raqam",
            example = "41006673910061",
            required = true,
            minLength = 14,
            maxLength = 14
    )
    @NotNull(message = "pinfl - majburiy parametr (bo'sh bo'lmasligi kerak)")
    @Size(min = 14, max = 14, message = "pinfl - faqat 14 ta raqamdan iborat bo'lishi kerak")
    @Pattern(regexp = "^\\d{14}$", message = "pinfl - faqat 14 ta raqamlardan iborat bo'lishi kerak harflar mumkin emas")
    @JsonProperty("pinfl")
    private String pinfl;
}
