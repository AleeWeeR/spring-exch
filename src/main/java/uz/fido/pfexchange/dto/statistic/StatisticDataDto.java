package uz.fido.pfexchange.dto.statistic;

import lombok.*;
import uz.fido.pfexchange.dto.MegaDataDto;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticDataDto {
    private String year;
    private String month;
    private String tin;
    private String coato;
    private List<MegaDataDto> megadata;
}
