package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.MinyustFamilyBatchResponseDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.service.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.utils.MinyustFamilyBatchStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("pf/pf/minyust/family/batch")
public class MinyustFamilyController {

    private final MinyustFamilyBatchRequestProcessor processor;
    private volatile boolean isProcessing = false;

    @PostMapping( "send-by-pnfl")
    public ResponseEntity<ResponseWrapperDto> startProcessingByPnfl() {
        if (isProcessing) {
            MinyustFamilyBatchResponseDto response = new MinyustFamilyBatchResponseDto();
            response.setStatus(MinyustFamilyBatchStatus.ALREADY_RUNNING);
            response.setMessage("Batch processing is already in progress");
            return ResponseEntity.ok(
                    ResponseWrapperDto.builder().data(response).build()
            );
        }

        isProcessing = true;

        new Thread(() -> {
            try {
                processor.processAllPendingRequests();
            } finally {
                isProcessing = false;
            }
        }).start();

        MinyustFamilyBatchResponseDto response = new MinyustFamilyBatchResponseDto();
        response.setStatus(MinyustFamilyBatchStatus.STARTED);
        response.setMessage("Batch processing is started");
        response.setPendingCount(processor.getPendingCount());

        return ResponseEntity.ok(
                ResponseWrapperDto.builder().data(response).build()
        );
    }

    @GetMapping("/status")
    public ResponseEntity<ResponseWrapperDto> getStatus() {
        MinyustFamilyBatchResponseDto response = new MinyustFamilyBatchResponseDto();
        response.setStatus(MinyustFamilyBatchStatus.PROCESSING);
        response.setPendingCount(processor.getPendingCount());
        return ResponseEntity.ok(
                ResponseWrapperDto.builder().data(response).build()
        );
    }

}
