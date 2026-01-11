package uz.fido.pfexchange.dto.mip.paytype;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipPayTypeChangeResponseDto {

    private Integer result;
    private String msg;
    private Long wsId;
}
