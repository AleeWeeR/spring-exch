package uz.fido.pfexchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import uz.fido.pfexchange.config.Constants;
import uz.fido.pfexchange.dto.PfExchangeStatisticDataDto;
import uz.fido.pfexchange.dto.PfExchangeStatisticsReqDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.StatisticsPostDto;
import uz.fido.pfexchange.service.PfExchangeStatisticDataService;
import uz.fido.pfexchange.utils.ResponseBuilder;

import java.io.IOException;
import java.sql.SQLException;


@RestController
@RequestMapping("api/v1/pf/statistics")
@RequiredArgsConstructor
public class PfExchangeStatistics {

    private final PfExchangeStatisticDataService pfExchangeStatisticDataService;

    @PostMapping(value = "get-token",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ResponseWrapperDto<?>> GetToken(@RequestBody String body) {
        RestClient restClient = RestClient.create();
        String url = "https://api.siat.stat.uz/integration/token/";

        try {
            Object data = restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)   // Body JSON ekanligini aytamiz
                    .body(body)                        // Body yuboramiz
                    .retrieve()
                    .body(Object.class);
            return ResponseEntity.ok(ResponseWrapperDto.builder().data(data).build());

        } catch (RestClientResponseException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message(ex.getResponseBodyAsString())
                            .build());
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message(e.getMessage())
                            .build()
                    );
        }

    }

    @PostMapping(value = "send-by-district",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ResponseWrapperDto<?>> SendByRegion(@RequestBody StatisticsPostDto statisticsPostDto) {
        RestClient restClient = RestClient.create();
        String url = "https://api.siat.stat.uz/acquisition/import/";

        try {

            Object data = restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + statisticsPostDto.getToken())
                    .body(statisticsPostDto.getData())
                    .retrieve()
                    .body(Object.class);

            return ResponseEntity.ok(ResponseWrapperDto.builder().data(data).build());

        } catch (RestClientResponseException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message(ex.getResponseBodyAsString())
                            .build());
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseWrapperDto.builder()
                            .code(Constants.ERROR)
                            .message(e.getMessage())
                            .build()
                    );
        }

    }

    @PreAuthorize(value = "hasAnyAuthority('GET_STATISTICS')")
    @PostMapping(value = "/get-data",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ResponseWrapperDto<PfExchangeStatisticDataDto>> getStatistics(@Valid @RequestBody PfExchangeStatisticsReqDto req) throws SQLException, IOException {
        return ResponseBuilder.ok(pfExchangeStatisticDataService.getStatistics(req.getPeriod(), req.getCoato()));
    }
}