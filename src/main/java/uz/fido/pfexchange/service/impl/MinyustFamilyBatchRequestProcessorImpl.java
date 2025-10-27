package uz.fido.pfexchange.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyItemDto;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyRequestDto;
import uz.fido.pfexchange.dto.minyust.MinyustFamilyResponseDto;
import uz.fido.pfexchange.entity.minyust.PfWomen;
import uz.fido.pfexchange.entity.minyust.PfWomenChildrenInf;
import uz.fido.pfexchange.repository.minyust.PfExchangeMinyustFamilyInfRepository;
import uz.fido.pfexchange.repository.minyust.PfWomenChildrenInfRepository;
import uz.fido.pfexchange.repository.minyust.PfWomenRepository;
import uz.fido.pfexchange.service.MinyustFamilyBatchRequestProcessor;
import uz.fido.pfexchange.utils.MinyustFamilyStatus;
import uz.fido.pfexchange.utils.Utils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinyustFamilyBatchRequestProcessorImpl
    implements MinyustFamilyBatchRequestProcessor {

    private final PfWomenRepository repository;
    private final PfWomenChildrenInfRepository childrenRepository;
    private final PfExchangeMinyustFamilyInfRepository minyustFamilyInfRepository;
    private final RestClient restClient = RestClient.create();

    private static final Integer DEFAULT_GENDER = 0;
    private static final Long DEFAULT_ID = 0L;
    private static final LocalDate DEFAULT_DATE = LocalDate.of(1900, 1, 1);
    private static final int REQUESTS_PER_SECOND = 50;
    private static final long DELAY_MS = 1000 / REQUESTS_PER_SECOND;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String URL =
        "http://10.190.24.138:96/api/ZagsToMinFin/GetFamily";

    @Override
    public void processAllPendingRequests() {
        log.info("Starting batch processing...");

        int batchSize = 1000;
        int totalProcessed = 0;
        int totalSuccess = 0;
        int totalFailed = 0;
        List<String> currentExchangeChildren;
        List<String> currentSavedChildren;
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
                        if (
                            response.getItems() != null &&
                            !response.getItems().isEmpty()
                        ) {
                            for (MinyustFamilyItemDto item : response.getItems()) {
                                PfWomenChildrenInf childRecord = mapToEntity(
                                    item,
                                    request
                                );
                                childrenRepository.save(childRecord);
                            }
                        }

                        request.setStatus(MinyustFamilyStatus.COMPLETED);
                        request.setRequestDate(LocalDateTime.now());
                        request.setDataIn(response.toString());

                        if (
                            response.getItems() != null &&
                            !response.getItems().isEmpty()
                        ) {
                            currentExchangeChildren = response
                                .getItems()
                                .stream()
                                .filter(
                                    member ->
                                        member.getM_pnfl() != null &&
                                        member
                                            .getM_pnfl()
                                            .equals(request.getPinpp())
                                )
                                .map(MinyustFamilyItemDto::getPnfl)
                                .toList();
                            currentSavedChildren = minyustFamilyInfRepository
                                .getLatestMinyustByPinpp(request.getPinpp())
                                .orElse(null);

                            if (currentSavedChildren == null) {
                                request.setStatus(
                                    MinyustFamilyStatus.NOT_FOUND
                                );
                            } else if (
                                currentExchangeChildren.size() !=
                                    currentSavedChildren.size() ||
                                !new HashSet<>(
                                    currentExchangeChildren
                                ).containsAll(currentSavedChildren)
                            ) {
                                request.setStatus(
                                    MinyustFamilyStatus.DIFFERENT
                                );
                            }
                        }
                    } else {
                        request.setStatus(MinyustFamilyStatus.FAILED);
                        request.setRequestDate(LocalDateTime.now());
                        request.setDataErr(
                            "Result code: " +
                                response.getResult_code() +
                                ", Message: " +
                                response.getResult_message()
                        );
                    }

                    repository.save(request);

                    totalSuccess++;
                } catch (Exception e) {
                    log.error(
                        "Failed to process request {}: {}",
                        request.getId(),
                        e.getMessage()
                    );

                    request.setStatus(MinyustFamilyStatus.FAILED);
                    request.setDataErr(e.getMessage());
                    repository.save(request);

                    totalFailed++;
                }

                totalProcessed++;

                rateLimitDelay();

                if (totalProcessed % 100 == 0) {
                    log.info(
                        "Progress: {} processed (Success: {}, Failed: {})",
                        totalProcessed,
                        totalSuccess,
                        totalFailed
                    );
                }
            }
        }

        log.info(
            "Batch processing completed. Total: {}, Success: {}, Failed: {}",
            totalProcessed,
            totalSuccess,
            totalFailed
        );
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

    private MinyustFamilyResponseDto callExternalService(
        MinyustFamilyRequestDto requestDto
    ) {
        try {
            log.info(
                "Calling external service: {} with body: {}",
                URL,
                requestDto
            );

            return restClient
                .post()
                .uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestDto)
                .retrieve()
                .onStatus(
                    HttpStatusCode::is4xxClientError,
                    (request, response) -> {
                        log.error("4xx error: {}", response.getStatusCode());
                        throw new IllegalArgumentException(
                            "Client error: " + response.getStatusCode()
                        );
                    }
                )
                .onStatus(
                    HttpStatusCode::is5xxServerError,
                    (request, response) -> {
                        log.error("5xx error: {}", response.getStatusCode());
                        throw new IllegalStateException(
                            "Server error: " + response.getStatusCode()
                        );
                    }
                )
                .body(MinyustFamilyResponseDto.class);
        } catch (ResourceAccessException e) {
            log.error("I/O error connecting to {}: {}", URL, e.getMessage(), e);
            throw new RuntimeException(
                "Failed to connect to external service",
                e
            );
        }
    }

    private PfWomenChildrenInf mapToEntity(
        MinyustFamilyItemDto dto,
        PfWomen pfWomen
    ) {
        return PfWomenChildrenInf.builder()
            .pfWomen(pfWomen)
            .pinpp(dto.getPnfl())
            .surname(dto.getSurname())
            .name(dto.getName())
            .patronym(dto.getPatronym())
            .birthDate(parseDate(dto.getBirth_date()))
            .gender(parseGender(dto.getGender_code()))
            .regNumber(dto.getDoc_num())
            .regDate(parseDate(dto.getDoc_date()))
            .recvZagsId(parseLong(dto.getBranch()))
            .certificateSeria(dto.getCert_series())
            .certificateNumber(dto.getCert_number())
            .certificateDate(parseDate(dto.getCert_birth_date()))
            .fatherPin(dto.getF_pnfl())
            .fatherSurnameLatin(dto.getF_family())
            .fatherNameLatin(dto.getF_first_name())
            .fatherPatronymLatin(dto.getF_patronym())
            .fatherBirthDate(parseDate(dto.getF_birth_day()))
            .motherPin(dto.getM_pnfl())
            .motherSurnameLatin(dto.getM_family())
            .motherNameLatin(dto.getM_first_name())
            .motherPatronymLatin(dto.getM_patronym())
            .motherBirthDate(parseDate(dto.getM_birth_day()))
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

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return DEFAULT_DATE;
        }

        try {
            String normalizedDateString = Utils.normalizeDateString(dateString);
            return LocalDate.parse(normalizedDateString, formatter);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.error("Failed to parse date string: {}", dateString, e);
            return DEFAULT_DATE;
        }
    }

    private Integer parseGender(String genderCode) {
        if (genderCode == null || genderCode.isEmpty()) {
            return DEFAULT_GENDER;
        }

        try {
            return Integer.parseInt(genderCode.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to parse gender code: {}", genderCode, e);
            return DEFAULT_GENDER;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return DEFAULT_ID;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.error("Failed to parse long value: {}", value, e);
            return DEFAULT_ID;
        }
    }
}
