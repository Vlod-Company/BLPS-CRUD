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
import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.controller.dto.response.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.service.PaymentCallbackProcessingService;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;

import java.net.URI;

@RestController
@RequestMapping("/api/internal-purchases")
@RequiredArgsConstructor
@Tag(name = "Internal Purchases")
public class InternalPurchaseController {

    private final InternalPurchaseService internalPurchaseService;
    private final PaymentCallbackProcessingService callbackProcessingService;

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid StartPurchaseRequest request) {
        PaymentRedirectResponse redirect = internalPurchaseService.startInternalPurchase(request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirect.redirectUrl()))
                .build();
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> callback(@RequestBody @Valid PaymentCallbackRequest request) {
        callbackProcessingService.handleCallback(request);
        return ResponseEntity.accepted().build();
    }
}
