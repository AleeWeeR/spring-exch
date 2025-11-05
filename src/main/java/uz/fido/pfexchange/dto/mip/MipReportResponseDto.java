package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class MipReportResponseDto {

    private int result;
    private String msg;
    private Long wsId;
    private String fio;
    private String sex;
    @JsonProperty("b_date")
    private String bDate;
    private List<MipPensionDto> pension;
}
