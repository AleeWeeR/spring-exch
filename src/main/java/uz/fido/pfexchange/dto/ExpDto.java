package uz.fido.pfexchange.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpDto {

    private String startDate;
    private String endDate;
    private String workExp;
    private String received;
    
}
