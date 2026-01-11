package uz.fido.pfexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.dto.statistic.StatisticDataDto;
import uz.fido.pfexchange.dto.ResponseWrapperDto;
import uz.fido.pfexchange.dto.statistic.StatisticsReqDto;
import uz.fido.pfexchange.service.StatisticDataService;
import uz.fido.pfexchange.utils.ResponseBuilder;
import static uz.fido.pfexchange.config.Authority.Codes.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pf/statistics")
@Tag(
    name = "Statistika",
    description = "Pensiya fondi almashinuv statistikasini olish API'lari"
)
public class StatisticController {

    private final StatisticDataService statisticDataService;

    @PostMapping("/get-data")
    @PreAuthorize("hasAuthority('" + GET_STATISTICS + "')")
    @Operation(
        summary = "Statistika ma'lumotlarini olish",
        description = "Berilgan davr va COATO bo'yicha pensiya fondi almashinuv statistikasini qaytaradi."
    )
    public ResponseEntity<ResponseWrapperDto<StatisticDataDto>> getStatistics(@Valid @RequestBody StatisticsReqDto request) {
        
        StatisticDataDto result = statisticDataService.getStatistics(request.getPeriod(), request.getCoato());
        
        return ResponseBuilder.ok(result);
    }
}