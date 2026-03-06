package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.controller.dto.ProcessPaymentRequest;
import ru.gigasigma.blpscrud.controller.dto.StartPurchaseRequest;
import ru.gigasigma.blpscrud.controller.dto.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.RedirectResponse;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

public interface PurchaseWorkflowService {
    WorkflowResult startInternalPurchase(StartPurchaseRequest request);
    WorkflowResult startExternalPurchase(StartPurchaseRequest request);
    RedirectResponse generateRedirectLink(ExternalRedirectRequest request);
    WorkflowResult completeExternalBooking(ExternalBookingCallbackRequest request);
    PaymentRedirectResponse generatePaymentRedirectLink(Long orderId);
    WorkflowResult handlePaymentCallback(PaymentCallbackRequest request);
    WorkflowResult processInternalPayment(Long orderId, ProcessPaymentRequest paymentRequest);
    WorkflowResult confirmExternalPayment(Long orderId);
    WorkflowResult cancelOrder(Long orderId);
}
