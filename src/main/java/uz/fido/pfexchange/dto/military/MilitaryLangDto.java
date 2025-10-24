package uz.fido.pfexchange.dto.military;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public class MilitaryLangDto {

    private String ru;
    private String uz;
    private String en;
    private String cy;
}
