package uz.fido.pfexchange.dto.mib;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for debt cancellation
 * Used to trigger debt cancellation for a person or application
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Qarzni bekor qilish uchun so'rov ob'ekti")
public class DebtCancellationRequestDto {

    @Schema(
            description = "External ID (inventory_id) - MIB tizimidagi qarzdorlik identifikatori",
            example = "123456",
            required = true
    )
    @NotNull(message = "external_id - majburiy parametr")
    @Positive(message = "external_id - musbat son bo'lishi kerak")
    @JsonProperty("external_id")
    private Long externalId;
}
