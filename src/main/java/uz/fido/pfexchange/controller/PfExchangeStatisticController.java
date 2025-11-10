package uz.fido.pfexchange.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.fido.pfexchange.dto.PfExchangeStatisticDataDto;
import uz.fido.pfexchange.dto.PfExchangeStatisticsReqDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.service.PfExchangeStatisticDataService;
import uz.fido.pfexchange.utils.ResponseBuilder;

import java.io.IOException;
import java.sql.SQLException;


@RestController
@RequestMapping("api/v1/pf/statistics")
@RequiredArgsConstructor
public class PfExchangeStatisticController {

    private final PfExchangeStatisticDataService pfExchangeStatisticDataService;

    @PreAuthorize(value = "hasAnyAuthority('GET_STATISTICS')")
    @PostMapping(value = "/get-data",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
    )
    public ResponseEntity<ResponseWrapperDto<PfExchangeStatisticDataDto>> getStatistics(@Valid @RequestBody PfExchangeStatisticsReqDto req) throws SQLException, IOException {
        return ResponseBuilder.ok(pfExchangeStatisticDataService.getStatistics(req.getPeriod(), req.getCoato()));
    }
}