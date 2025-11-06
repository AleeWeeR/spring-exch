package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MipPensionDto {

    private String type;
    private String condition;
    private String total;
    private String reqExp;
    private String salary;
    private String amount;
    private String base;
    private String increase;
    private String appDate;
    private String penAmount;
    private List<MipExpDto> staj;
    private List<MipSalaryDto> ishHaqi;
}
