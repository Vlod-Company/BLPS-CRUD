package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.controller.dto.PaymentRedirectResponse;

public interface PaymentService {

    PaymentRedirectResponse generatePaymentRedirectLink(Long orderId);
}
