package uz.fido.pfexchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.fido.pfexchange.entity.PfSExchangeStatus;
import uz.fido.pfexchange.service.PfSExchangeStatusesService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("pf")
@RequiredArgsConstructor
public class PfSExchangeStatusController {

    private final PfSExchangeStatusesService pfSExchangeStatusesService;

    @GetMapping("get-pf-exchange-status/{code}")
    public ResponseEntity<?> getPfSExchangeStatus(@PathVariable String code) {
        Optional<PfSExchangeStatus> optional = pfSExchangeStatusesService.getByCode(code);
        if (optional.isPresent()) {
            return new ResponseEntity<>(optional.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>("No data available", HttpStatus.NOT_FOUND);
    }

    @GetMapping("get-pf-exchange-statuses")
    public ResponseEntity<List<PfSExchangeStatus>> getPfSExchangeStatuses() {

        return ResponseEntity.ok(pfSExchangeStatusesService.getAll());
    }
}
