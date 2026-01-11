package uz.fido.pfexchange.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PensionDto {

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
    private List<ExpDto> staj;
    private List<SalaryDto> ishHaqi;
}
