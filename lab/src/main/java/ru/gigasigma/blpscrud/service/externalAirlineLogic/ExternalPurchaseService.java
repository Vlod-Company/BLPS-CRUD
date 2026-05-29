package ru.gigasigma.blpscrud.service.externalAirlineLogic;

import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.response.RedirectResponse;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

public interface ExternalPurchaseService {
    RedirectResponse generateRedirectLink(ExternalRedirectRequest request);
    WorkflowResult completeExternalBooking(ExternalBookingCallbackRequest request);
}