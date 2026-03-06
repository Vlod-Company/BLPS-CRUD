package ru.gigasigma.blpscrud.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.service.PurchaseWorkflowService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PurchaseWorkflowService purchaseWorkflowService;

    @PostMapping("/redirect/{orderId}")
    public PaymentRedirectResponse redirect(@PathVariable Long orderId) {
        return purchaseWorkflowService.generatePaymentRedirectLink(orderId);
    }

    @PostMapping("/callback")
    public WorkflowResult callback(@RequestBody PaymentCallbackRequest request) {
        return purchaseWorkflowService.handlePaymentCallback(request);
    }
}
