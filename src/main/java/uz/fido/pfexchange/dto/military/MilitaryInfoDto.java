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
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MilitaryInfoDto {

    private List<MilitaryServiceDto> service;
    private LocalDate startDate;
    private LocalDate endDate;
}
