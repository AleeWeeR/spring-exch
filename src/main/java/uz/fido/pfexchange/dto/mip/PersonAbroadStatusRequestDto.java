package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Person abroad status so'rovi uchun DTO
 * Request DTO for person abroad status query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pensiya oluvchi holatini tekshirish uchun so'rov ob'ekti")
public class PersonAbroadStatusRequestDto {

    @Schema(
            description = "So'rov ma'lumotlari",
            required = true
    )
    @NotNull(message = "Data - majburiy parametr (bo'sh bo'lmasligi kerak)")
    @Valid
    @JsonProperty("Data")
    private PersonAbroadStatusDataDto data;
}
