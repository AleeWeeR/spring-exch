package uz.fido.pfexchange.dto.military;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.KebabCaseStrategy.class)
public class MilitaryResponseDataDto {

    private String pin;
    private String firstName;
    private String lastName;
    private String patronymic;
    private LocalDate birthDate;
    private List<MilitaryInfoDto> militaryServices;
}
