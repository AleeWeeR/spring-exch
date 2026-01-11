package uz.fido.pfexchange.service.mip;

import uz.fido.pfexchange.dto.mip.push.MipPushDeliveryStatisticsDailyReponseDto;
import uz.fido.pfexchange.dto.mip.push.MipPushDetailedReportResponseDto;
import uz.fido.pfexchange.dto.mip.push.MipPushPensionRequestDataDto;

import java.util.List;

public interface MipPushService {

    String token();

    MipPushDetailedReportResponseDto deliveryDetailedReport(String uuid);

    List<MipPushDeliveryStatisticsDailyReponseDto> deliveryStatisticsDaily();

    String ping();

    String push(MipPushPensionRequestDataDto requestDto);

    String pushAsync(MipPushPensionRequestDataDto requestDto);
}
