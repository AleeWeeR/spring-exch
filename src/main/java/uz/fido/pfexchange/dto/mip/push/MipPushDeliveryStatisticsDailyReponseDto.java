package uz.fido.pfexchange.dto.mip.push;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MipPushDeliveryStatisticsDailyReponseDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Integer totalSubscribers;
    private Integer processesMessages;
    private Integer successfulDeliveries;
    private Integer failedDeliveries;
    private Integer pendingDeliveries;
}
