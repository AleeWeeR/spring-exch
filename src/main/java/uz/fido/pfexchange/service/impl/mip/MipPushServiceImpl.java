package uz.fido.pfexchange.service.impl.mip;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uz.fido.pfexchange.config.RestClientWrapper;
import uz.fido.pfexchange.dto.mip.push.MipPushDeliveryStatisticsDailyReponseDto;
import uz.fido.pfexchange.dto.mip.push.MipPushDetailedReportResponseDto;
import uz.fido.pfexchange.dto.mip.push.MipPushPensionRequestDataDto;
import uz.fido.pfexchange.dto.mip.push.MipPushRequestDto;
import uz.fido.pfexchange.entity.PushInfo;
import uz.fido.pfexchange.repository.PushInfoRepository;
import uz.fido.pfexchange.security.JwtService;
import uz.fido.pfexchange.service.mip.MipPushService;
import uz.fido.pfexchange.utils.PushStatus;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MipPushServiceImpl implements MipPushService {

    private final JwtService jwtService;
    private final RestClientWrapper restClientWrapper;
    private final PushInfoRepository pushInfoRepository;
    private final PushPersistenceService persistenceService;

    private final String DAILY_STATS_URL =
            "https://pushservice.egov.uz/v3/app/mq/publisher/fetch-daily-delivery-statistics";
    private final String DETAILED_REPORT_URL =
            "https://pushservice.egov.uz/v3/app/mq/publisher/fetch-delivery-detailed-report";
    private final String PING_URL = "https://pushservice.egov.uz/v3/app/mq/ping";
    private final String PUSH_URL = "https://pushservice.egov.uz/v3/app/mq/receive";

    @Override
    public String token() {
        return jwtService.generateAsymmetricToken();
    }

    @Override
    public MipPushDetailedReportResponseDto deliveryDetailedReport(String uuid) {
        String token = jwtService.generateAsymmetricToken();
        return restClientWrapper.get(
                DETAILED_REPORT_URL + "/" + uuid,
                token,
                "MIP Push",
                MipPushDetailedReportResponseDto.class);
    }

    @Override
    public String ping() {
        String token = jwtService.generateAsymmetricToken();
        return restClientWrapper.get(PING_URL, token, "MIP Push", String.class);
    }

    @Override
    public String push(MipPushPensionRequestDataDto requestDto) {

        PushInfo savedInfo = persistenceService.createPushInfo(requestDto);

        try {
            String token = jwtService.generateAsymmetricToken();

            MipPushRequestDto<MipPushPensionRequestDataDto> request = new MipPushRequestDto<>();
            request.setCorrelationId(savedInfo.getCorrelationId());
            request.setData(requestDto);
            request.getData().setWsId(savedInfo.getWsId());

            String response =
                    restClientWrapper.post(
                            PUSH_URL, token, request, "MIP Push Service", String.class);

            persistenceService.updateStatus(savedInfo.getWsId(), PushStatus.SENT);
            return response;

        } catch (Exception e) {
            persistenceService.updateStatus(savedInfo.getWsId(), PushStatus.FAILED);
            throw e;
        }
    }

    @Override
    public String pushAsync(MipPushPensionRequestDataDto requestDto) {

        PushInfo savedInfo = persistenceService.createPushInfo(requestDto);

        processPushAsync(savedInfo.getWsId(), requestDto);

        return savedInfo.getCorrelationId();
    }

    @Override
    public List<MipPushDeliveryStatisticsDailyReponseDto> deliveryStatisticsDaily() {
        String token = jwtService.generateAsymmetricToken();
        return restClientWrapper.get(
                DAILY_STATS_URL,
                token,
                "MIP Push",
                new ParameterizedTypeReference<
                        List<MipPushDeliveryStatisticsDailyReponseDto>>() {});
    }

    @Async("pushExecutor")
    public void processPushAsync(Long pushInfoId, MipPushPensionRequestDataDto requestDto) {

        try {
            String token = jwtService.generateAsymmetricToken();

            PushInfo info = pushInfoRepository.findById(pushInfoId).orElseThrow();

            MipPushRequestDto<MipPushPensionRequestDataDto> request = new MipPushRequestDto<>();
            request.setCorrelationId(info.getCorrelationId());
            request.setData(requestDto);
            request.getData().setWsId(info.getWsId());

            String response = restClientWrapper.post(PUSH_URL, token, request, "MIP Push Service", String.class);

            persistenceService.updateStatus(
                    pushInfoId,
                    response.contains("accepted") ? PushStatus.SENT : PushStatus.FAILED);

        } catch (Exception e) {
            persistenceService.updateStatus(pushInfoId, PushStatus.FAILED);
            log.error("Async push failed: {}", pushInfoId, e);
        }
    }

    public void doPush(Long pushInfoId, MipPushPensionRequestDataDto requestDto) {

        PushInfo pushInfo = pushInfoRepository.findById(pushInfoId).orElseThrow();

        String token = jwtService.generateAsymmetricToken();

        MipPushRequestDto<MipPushPensionRequestDataDto> request = new MipPushRequestDto<>();
        request.setCorrelationId(pushInfo.getCorrelationId());
        request.setData(requestDto);
        request.getData().setWsId(pushInfo.getWsId());

        String response =
                restClientWrapper.post(PUSH_URL, token, request, "MIP Push Service", String.class);

        if (response.contains("accepted")) {
            updateStatus(pushInfoId, PushStatus.SENT);
        } else {
            updateStatus(pushInfoId, PushStatus.FAILED);
        }
    }

    @Transactional
    private PushInfo createPushInfo(MipPushPensionRequestDataDto requestDto) {
        PushInfo pushInfo = new PushInfo();
        pushInfo.setCorrelationId(UUID.randomUUID().toString());
        pushInfo.setPinpp(requestDto.getPinfl());
        pushInfo.setPensType(requestDto.getType());
        pushInfo.setGrounds(requestDto.getGrounds());
        pushInfo.setStatus(PushStatus.CREATED);
        return pushInfoRepository.save(pushInfo);
    }

    @Transactional
    public void updateStatus(Long id, PushStatus status) {
        PushInfo info = pushInfoRepository.findById(id).orElseThrow();
        info.setStatus(status);
    }
}
