package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Flights")
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    @Operation(summary = "Search flights", description = "Returns flights for a route, date, and passenger count.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flights found", content = @Content(array = @ArraySchema(schema = @Schema(implementation = FlightResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public List<FlightResponse> search(
            @Parameter(description = "Departure airport IATA code", example = "LED", required = true)
            @RequestParam(name = "from") @NotBlank(message = "Parameter 'from' is required") String from,
            @Parameter(description = "Arrival airport IATA code", example = "SVO", required = true)
            @RequestParam(name = "to") @NotBlank(message = "Parameter 'to' is required") String to,
            @Parameter(description = "Flight date in ISO format", example = "2026-03-24", required = true)
            @RequestParam @NotNull(message = "Parameter 'date' is required") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Number of passengers", example = "2")
            @RequestParam(name = "passengers", defaultValue = "1") @Positive(message = "passengers must be greater than 0") Integer passengers
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
    @Operation(summary = "Get flight by id", description = "Returns full details for a single flight.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flight found", content = @Content(schema = @Schema(implementation = FlightResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid identifier", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Flight not found", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public FlightResponse getById(
            @Parameter(description = "Flight identifier", example = "105", required = true)
            @PathVariable @Positive(message = "id must be a positive number") Long id
    ) {
        return FlightResponse.fromEntity(flightService.getById(id));
    }
}
