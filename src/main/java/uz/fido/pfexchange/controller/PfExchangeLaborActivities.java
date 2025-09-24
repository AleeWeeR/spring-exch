package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;


@RestController
@RequestMapping("pf/pf/labor")
@RequiredArgsConstructor
public class PfExchangeLaborActivities {


    @PostMapping("setActivities")
    public ResponseEntity<?> getPfSExchangeStatus(@RequestBody String body) {
        RestClient restClient = RestClient.create();
        String url = "http://10.50.71.34:8400/PF/pf/labor/setActivities.jsp?test=123";

        try {

            String requestBody = """
            {
              "pinpp": "1234567890",
              "status": "ACTIVE"
            }
            """;

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

}