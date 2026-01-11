package uz.fido.pfexchange.dto.mip.push;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipPushDeliveriesDto {

    private String status;
    private List<MipPushDeliveryInfoDto> info;
}
