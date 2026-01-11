package uz.fido.pfexchange.dto.abroad;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.fido.pfexchange.utils.validation.FirstCheck;
import uz.fido.pfexchange.utils.validation.SecondCheck;

/**
 * Person abroad status ma'lumotlari uchun DTO
 * Data DTO for person abroad status query
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonAbroadStatusDataDto {

    @NotNull(groups = FirstCheck.class)
    @Digits(integer = 12, fraction = 0)
    @JsonProperty("ws_id")
    private Long wsId;


    @Size(min = 14, max = 14)
    @NotBlank(groups = FirstCheck.class)
    @Pattern(regexp = "\\d{14}", message = "{pinfl.pattern}", groups = SecondCheck.class)
    @JsonProperty("pinfl")
    private String pinfl;

}
