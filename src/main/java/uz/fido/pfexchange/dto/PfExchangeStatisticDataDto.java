package uz.fido.pfexchange.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PfExchangeStatisticDataDto {
    private String year;
    private String month;
    private String tin;
    private String coato;
    private List<MegaDataDto> megadata;
}
