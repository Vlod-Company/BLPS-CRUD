package ru.gigasigma.blpscrud.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gigasigma.blpscrud.controller.dto.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.ExternalRedirectRequest;
import ru.gigasigma.blpscrud.controller.dto.RedirectResponse;
import ru.gigasigma.blpscrud.service.PurchaseWorkflowService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final PurchaseWorkflowService purchaseWorkflowService;

    @PostMapping("/redirect")
    public RedirectResponse generateRedirect(@RequestBody ExternalRedirectRequest request) {
        return purchaseWorkflowService.generateRedirectLink(request);
    }

    @PostMapping("/external/callback")
    public WorkflowResult externalCallback(@RequestBody ExternalBookingCallbackRequest request) {
        return purchaseWorkflowService.completeExternalBooking(request);
    }
}
