package ru.gigasigma.blpscrud.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.RedirectResponse;
import ru.gigasigma.blpscrud.service.ExternalPurchaseService;
import ru.gigasigma.blpscrud.service.FlightQueryService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.util.ExternalPurchaseServiceFactory;

import java.net.URI;

@RestController
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final ExternalPurchaseServiceFactory externalPurchaseServiceFactory;
    private final FlightQueryService flightQueryService;

    @PostMapping("/redirect")
    public ResponseEntity<Void> generateRedirect(@RequestBody ExternalRedirectRequest request) {
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(request.provider());
        RedirectResponse redirect = service.generateRedirectLink(request);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @PostMapping("/external/callback")
    public WorkflowResult externalCallback(@RequestBody ExternalBookingCallbackRequest request) {
        String iata = flightQueryService.getById(request.flightId()).getAirline().getIataCode();
        ExternalPurchaseService service = externalPurchaseServiceFactory.getService(iata);
        return service.completeExternalBooking(request);
    }
}
