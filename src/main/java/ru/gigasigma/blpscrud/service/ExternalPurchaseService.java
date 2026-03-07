package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.controller.dto.*;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

public interface ExternalPurchaseService {
    RedirectResponse generateRedirectLink(ExternalRedirectRequest request);
    WorkflowResult completeExternalBooking(ExternalBookingCallbackRequest request);
}