package uz.fido.pfexchange.service.minyust;

import uz.fido.pfexchange.dto.minyust.ProcessingState;

public interface MinyustFamilyContinuousProcessingService {
    boolean start();

    boolean stop();

    ProcessingState getState();

    boolean isRunning();
}
