package uz.fido.pfexchange.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryDto {

    private String year;
    private String month;
    private String salary;
}
