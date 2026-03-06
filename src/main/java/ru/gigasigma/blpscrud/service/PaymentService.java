package ru.gigasigma.blpscrud.service;

import ru.gigasigma.blpscrud.controller.dto.ProcessPaymentRequest;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.service.dto.PaymentResult;

public interface PaymentService {

    PaymentResult process(Order order, ProcessPaymentRequest request);
}
