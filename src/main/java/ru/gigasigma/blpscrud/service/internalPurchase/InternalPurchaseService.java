package ru.gigasigma.blpscrud.service.internalPurchase;

import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.response.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

public interface InternalPurchaseService {
    PaymentRedirectResponse startInternalPurchase(StartPurchaseRequest request);
    WorkflowResult handlePaymentCallback(PaymentCallbackRequest request);
    PaymentRedirectResponse generatePaymentRedirectLink(Long orderId);
}