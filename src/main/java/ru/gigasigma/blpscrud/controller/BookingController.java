package ru.gigasigma.blpscrud.controller;

import lombok.RequiredArgsConstructor;
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
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

import java.net.URI;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final ExternalPurchaseService externalPurchaseService;

    @PostMapping("/redirect")
    public ResponseEntity<Void> generateRedirect(@RequestBody ExternalRedirectRequest request) {
        RedirectResponse redirect = externalPurchaseService.generateRedirectLink(request);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @PostMapping("/external/callback")
    public WorkflowResult externalCallback(@RequestBody ExternalBookingCallbackRequest request) {
        return externalPurchaseService.completeExternalBooking(request);
    }
}
