package uz.fido.pfexchange.dto.military;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MilitaryRequestDto {

    @NotBlank
    @Size(min = 14, max = 14)
    private String pin;

    @NotBlank
    @Size(min = 14, max = 14)
    private String senderPin;

    @NotNull
    private Integer transactionId;

    @NotBlank
    @Size(min = 1, max = 255)
    private String purpose;

    @Size(min = 1, max = 255)
    private String consent;
}
