package uz.fido.pfexchange.dto.mip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MipReportResponseDto {

    private Integer result;
    private String msg;
    private Long wsId;
    private String fio;
    private String sex;
    private String b_date;
    private List<MipPensionDto> pension;
}
