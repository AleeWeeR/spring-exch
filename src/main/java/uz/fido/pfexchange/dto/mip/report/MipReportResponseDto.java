package uz.fido.pfexchange.dto.mip.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.*;
import uz.fido.pfexchange.dto.PensionDto;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MipReportResponseDto {

    private Integer result;
    private String msg;
    private Long wsId;
    private String fio;
    private String sex;
    private String b_date;
    private List<PensionDto> pension;
}
