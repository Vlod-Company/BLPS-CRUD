package ru.gigasigma.blpscrud.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.response.RedirectResponse;
import ru.gigasigma.blpscrud.service.FlightService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.ExternalPurchaseService;
import ru.gigasigma.blpscrud.util.ExternalPurchaseServiceFactory;

import java.net.URI;

@RestController
@RequestMapping("/api/external-purchases")
@RequiredArgsConstructor
@Tag(name = "External Purchases")
public class ExternalPurchaseController {

    private final ExternalPurchaseServiceFactory externalPurchaseServiceFactory;
    private final FlightService flightService;

    @PostMapping("/redirect")
    public ResponseEntity<Void> generateRedirect(@RequestBody @Valid ExternalRedirectRequest request) {
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(request.provider());
        RedirectResponse redirect = service.generateRedirectLink(request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @PostMapping("/callback")
    public WorkflowResult callback(@RequestBody @Valid ExternalBookingCallbackRequest request) {
        String iata = flightService.getById(request.flightId()).getAirline().getIataCode();
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(iata);
        return service.completeExternalBooking(request);
    }
}
