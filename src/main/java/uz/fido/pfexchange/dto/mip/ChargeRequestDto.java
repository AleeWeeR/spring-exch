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
 * Qarzdorlik ma'lumotlari uchun so'rov DTO
 * Request DTO for charge information queries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Qarzdorlik ma'lumotini so'rash uchun so'rov ob'ekti")
public class ChargeRequestDto {

    @Schema(
            description = "Veb-servis identifikatori (musbat butun son)",
            example = "12345",
            required = true
    )
    @NotNull(message = "ws_id - majburiy parametr (bo'sh bo'lmasligi kerak)")
    @Positive(message = "ws_id - musbat son bo'lishi kerak")
    @JsonProperty("ws_id")
    private Long wsId;

    @Schema(
            description = "Shaxsiy identifikatsiya raqami (pinfl) - 14 xonali raqam",
            example = "12345678901234",
            required = true,
            minLength = 14,
            maxLength = 14
    )
    @NotNull(message = "pinfl - majburiy parametr (bo'sh bo'lmasligi kerak)")
    @Size(min = 14, max = 14, message = "pinfl - 14 ta raqamdan iborat bo'lishi kerak")
    @Pattern(regexp = "^\\d{14}$", message = "pinfl - faqat raqamlardan iborat bo'lishi kerak")
    @JsonProperty("pinfl")
    private String pinfl;
}