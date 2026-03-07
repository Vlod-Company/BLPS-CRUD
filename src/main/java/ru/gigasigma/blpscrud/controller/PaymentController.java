package ru.gigasigma.blpscrud.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.service.InternalPurchaseService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final InternalPurchaseService purchaseService;

    @PostMapping("/callback")
    public WorkflowResult callback(@RequestBody @Valid PaymentCallbackRequest request) {
        return purchaseService.handlePaymentCallback(request);
    }
}
