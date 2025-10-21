package uz.fido.pfexchange.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.fido.pfexchange.dto.MinyustFamilyItemDto;
import uz.fido.pfexchange.dto.MinyustFamilyRequestDto;
import uz.fido.pfexchange.dto.MinyustFamilyResponseDto;
import uz.fido.pfexchange.entity.PfWomen;
import uz.fido.pfexchange.entity.PfWomenChildrenInf;
import uz.fido.pfexchange.repository.PfWomenChildrenInfRepository;
import uz.fido.pfexchange.repository.PfWomenRepository;
import uz.fido.pfexchange.service.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.utils.MinyustFamilyStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinyustFamilyBatchRequestProcessorImpl implements MinyustFamilyBatchRequestProcessor {

    private final PfWomenRepository repository;
    private final PfWomenChildrenInfRepository childrenRepository;
    private final RestClient restClient = RestClient.create();

    private static final int REQUESTS_PER_SECOND = 50;
    private static final long DELAY_MS = 1000 / REQUESTS_PER_SECOND;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String URL = "http://10.190.24.138:96/api/ZagsToMinFin/GetFamily";

    @Override
    public void processAllPendingRequests() {
        log.info("Starting batch processing...");

        int batchSize = 1000;
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalFailed = 0;
        long childrenCountRes;
        long childrenCountBase;
        int id = 1;

        while (true) {

            List<PfWomen> batch = repository.findByStatusWithLimit(
                    MinyustFamilyStatus.READY.name(),
                    batchSize
            );

            if (batch.isEmpty()) {
                log.info("No more READY records to process");
                break;
            }

            log.info("Processing batch of {} records", batch.size());

            for (PfWomen request : batch) {
                try {
                    request.setStatus(MinyustFamilyStatus.PROCESSING);
                    repository.save(request);

                    MinyustFamilyResponseDto response = callExternalService(
                            MinyustFamilyRequestDto.builder()
                                    .ID(String.valueOf(id++))
                                    .pnfl(request.getPinpp())
                                    .tin("20201210")
                                    .build()
                    );

                    if ("1".equals(response.getResult_code())) {

                        if (response.getItems() != null && !response.getItems().isEmpty()) {
                            for (MinyustFamilyItemDto item : response.getItems()) {
                                PfWomenChildrenInf childRecord = mapToEntity(item, request);
                                childrenRepository.save(childRecord);
                            }
                        }

                        request.setStatus(MinyustFamilyStatus.COMPLETED);
                        request.setRequestDate(LocalDateTime.now());
                        request.setDataIn(response.toString());

                        childrenCountRes = response.getItems().stream()
                                .filter(
                                        member -> LocalDate.parse(member.getM_birth_day(), formatter)
                                                .equals(request.getBirthDate())).count();
                        childrenCountBase = childrenRepository.countCaseDocsByClause(request.getApplicationId());

                        if (childrenCountRes != childrenCountBase) {
                            request.setStatus(MinyustFamilyStatus.DIFFERENT);
                        }

                    } else {
                        request.setStatus(MinyustFamilyStatus.FAILED);
                        request.setRequestDate(LocalDateTime.now());
                        request.setDataErr("Result code: " + response.getResult_code() +
                                ", Message: " + response.getResult_message());
                    }



                    repository.save(request);

                    totalSuccess++;

                } catch (Exception e) {
                    log.error("Failed to process request {}: {}", request.getId(), e.getMessage());

                    request.setStatus(MinyustFamilyStatus.FAILED);
                    request.setDataErr(e.getMessage());
                    repository.save(request);

                    totalFailed++;
                }

                totalProcessed++;

                rateLimitDelay();

                if (totalProcessed % 100 == 0) {
                    log.info("Progress: {} processed (Success: {}, Failed: {})",
                            totalProcessed, totalSuccess, totalFailed);
                }
            }
        }

        log.info("Batch processing completed. Total: {}, Success: {}, Failed: {}",
                totalProcessed, totalSuccess, totalFailed);
    }

    private void rateLimitDelay() {
        try {
            Thread.sleep(DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing interrupted");
            throw new RuntimeException("Processing interrupted", e);
        }
    }

    private MinyustFamilyResponseDto callExternalService(MinyustFamilyRequestDto requestDto) {
        try {
            log.info("Calling external service: {} with body: {}", URL, requestDto);

            return restClient
                    .post()
                    .uri(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("4xx error: {}", response.getStatusCode());
                        throw new IllegalArgumentException("Client error: " + response.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("5xx error: {}", response.getStatusCode());
                        throw new IllegalStateException("Server error: " + response.getStatusCode());
                    })
                    .body(MinyustFamilyResponseDto.class);

        } catch (ResourceAccessException e) {
            log.error("I/O error connecting to {}: {}", URL, e.getMessage(), e);
            throw new RuntimeException("Failed to connect to external service", e);
        }
    }

    private PfWomenChildrenInf mapToEntity(MinyustFamilyItemDto dto, PfWomen pfWomen) {
        return PfWomenChildrenInf.builder()
                .pfWomen(pfWomen)
                .pinpp(dto.getPnfl())
                .surname(dto.getSurname())
                .name(dto.getName())
                .patronym(dto.getPatronym())
                .birthDate(LocalDate.parse(dto.getBirth_date(), formatter))
                .gender(Integer.parseInt(dto.getGender_code()))
                .regNumber(dto.getDoc_num())
                .regDate(LocalDate.parse(dto.getDoc_date(), formatter))
                .recvZagsId(Long.parseLong(dto.getBranch()))
                .certificateSeria(dto.getCert_series())
                .certificateNumber(dto.getCert_number())
                .certificateDate(LocalDate.parse(dto.getCert_birth_date(), formatter))
                .fatherPin(dto.getF_pnfl())
                .fatherSurnameLatin(dto.getF_family())
                .fatherNameLatin(dto.getF_first_name())
                .fatherPatronymLatin(dto.getF_patronym())
                .fatherBirthDate(LocalDate.parse(dto.getF_birth_day(), formatter))
                .motherPin(dto.getM_pnfl())
                .motherSurnameLatin(dto.getM_family())
                .motherNameLatin(dto.getM_first_name())
                .motherPatronymLatin(dto.getM_patronym())
                .motherBirthDate(LocalDate.parse(dto.getM_birth_day(), formatter))
                .createdBy(-1L)
                .creationDate(LocalDate.now())
                .isActive("Y")
                .isAlive(dto.getLive_status())
                .build();
    }

    @Override
    public long getPendingCount() {
        return repository.countUnprocessed();
    }
}
