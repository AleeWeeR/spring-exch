package uz.fido.pfexchange.service.impl.mip;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uz.fido.pfexchange.dto.mip.push.MipPushPensionRequestDataDto;
import uz.fido.pfexchange.entity.PushInfo;
import uz.fido.pfexchange.repository.PushInfoRepository;
import uz.fido.pfexchange.utils.PushStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushPersistenceService {

    private final PushInfoRepository pushInfoRepository;

    @Transactional
    public PushInfo createPushInfo(MipPushPensionRequestDataDto dto) {
        PushInfo pushInfo = new PushInfo();
        pushInfo.setCorrelationId(UUID.randomUUID().toString());
        pushInfo.setPinpp(dto.getPinfl());
        pushInfo.setPensType(dto.getType());
        pushInfo.setGrounds(dto.getGrounds());
        pushInfo.setStatus(PushStatus.CREATED);
        return pushInfoRepository.save(pushInfo);
    }

    @Transactional
    public void updateStatus(Long id, PushStatus status) {
        PushInfo info = pushInfoRepository.findById(id).orElseThrow();
        info.setStatus(status);
    }
}
