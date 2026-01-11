package uz.fido.pfexchange.dto.mip.paytype;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipPayTypeResponseDto {

    private Integer result;
    private String msg;
    private Long wsId;
    private String fio;
    private String pensType;
    private Integer cashPercent;
    private String maxallaName;
    private Integer cardPercent;
    private String cardType;
    private String bankName;
    private Integer savingsPercent;
    private String savingsName;
}
