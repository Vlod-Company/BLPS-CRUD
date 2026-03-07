package ru.gigasigma.blpscrud.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.FlightResponse;
import ru.gigasigma.blpscrud.service.FlightService;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Validated
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public List<FlightResponse> search(
            @RequestParam(name = "from") @NotBlank(message = "Параметр from обязателен") String from,
            @RequestParam(name = "to") @NotBlank(message = "Параметр to обязателен") String to,
            @RequestParam @NotNull(message = "Параметр date обязателен") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "passengers", defaultValue = "1") @Positive(message = "passengers должен быть больше 0") Integer passengers
    ) {
        return flightService.search(
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
    public FlightResponse getById(@PathVariable @Positive(message = "id должен быть положительным числом") Long id) {
        return FlightResponse.fromEntity(flightService.getById(id));
    }
}
