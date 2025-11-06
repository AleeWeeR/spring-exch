package uz.fido.pfexchange.dto.mip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipFunctionResultDto {

    private Integer returnCode;
    private String jsonText;
}
