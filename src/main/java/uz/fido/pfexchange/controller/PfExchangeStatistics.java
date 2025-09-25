package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import uz.fido.pfexchange.dto.StatisticsPostDto;


@RestController
@RequestMapping("pf/pf/statistics")
@RequiredArgsConstructor
public class PfExchangeStatistics {


    @PostMapping("get-token")
    public ResponseEntity<?> GetToken(@RequestBody String body) {
        RestClient restClient = RestClient.create();
        String url = "https://10.190.0.178/integration/token/";

        try {

            return restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)   // Body JSON ekanligini aytamiz
                    .body(body)                        // Body yuboramiz
                    .retrieve()
                    .toEntity(String.class);

        } catch (RestClientResponseException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }

    }

    @PostMapping("send-by-district")
    public ResponseEntity<?> SendByRegion(@RequestBody StatisticsPostDto statisticsPostDto) {
        RestClient restClient = RestClient.create();
        String url = "https://10.190.0.178/acquisition/import/";

        try {

            return restClient
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + statisticsPostDto.getToken())
                    .body(statisticsPostDto.getData())
                    .retrieve()
                    .toEntity(String.class);

        } catch (RestClientResponseException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());
        } catch (Exception e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }

    }

}