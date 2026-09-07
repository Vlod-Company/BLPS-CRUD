package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.gigasigma.blpscrud.controller.dto.response.FlightResponse;
import ru.gigasigma.blpscrud.service.FlightService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Tag(name = "Flights")
@Slf4j
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public List<FlightResponse> search(
            @RequestParam(name = "from") @NotBlank(message = "Parameter 'from' is required") String from,
            @RequestParam(name = "to") @NotBlank(message = "Parameter 'to' is required") String to,
            @RequestParam @NotNull(message = "Parameter 'date' is required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "passengers", defaultValue = "1") @Positive(message = "passengers must be greater than 0") Integer passengers
    ) {
        log.info("Flight search request. from={}, to={}, date={}, passengers={}", from, to, date, passengers);
        List<FlightResponse> result = flightService.search(
                        from,
                        to,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay().minusSeconds(1),
                        passengers
                )
                .stream()
                .map(FlightResponse::fromEntity)
                .toList();
        log.info("Flight search completed. from={}, to={}, count={}", from, to, result.size());
        return result;
    }

    @GetMapping("/{id}")
    public FlightResponse getById(
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
        log.info("Flight getById request. id={}", id);
        FlightResponse response = FlightResponse.fromEntity(flightService.getById(id));
        log.info("Flight getById completed. id={}", id);
        return response;
    }
}
