package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.controller.dto.*;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

public interface InternalPurchaseService {
    PaymentRedirectResponse startInternalPurchase(StartPurchaseRequest request);
    WorkflowResult handlePaymentCallback(PaymentCallbackRequest request);
}