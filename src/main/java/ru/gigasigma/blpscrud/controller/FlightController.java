package ru.gigasigma.blpscrud.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.FlightResponse;
import ru.gigasigma.blpscrud.service.FlightQueryService;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightQueryService flightQueryService;

    @GetMapping
    public List<FlightResponse> search(
            @RequestParam(name = "from") String from,
            @RequestParam(name = "to") String to,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "passengers", defaultValue = "1") Integer passengers
    ) {
        return flightQueryService.search(
                        from,
                        to,
                        date.atStartOfDay(),
                        date.plusDays(1).atStartOfDay().minusSeconds(1),
                        passengers
                )
                .stream()
                .map(FlightResponse::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public FlightResponse getById(@PathVariable Long id) {
        return FlightResponse.fromEntity(flightQueryService.getById(id));
    }
}
