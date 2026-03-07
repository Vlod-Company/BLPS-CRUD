package ru.gigasigma.blpscrud.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.controller.dto.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.ProcessPaymentRequest;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.service.PaymentService;
import ru.gigasigma.blpscrud.service.dto.PaymentResult;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderService orderService;

    @Override
    public PaymentRedirectResponse generatePaymentRedirectLink(Long orderId) {
        Order order = orderService.getOrder(orderId);
        if (order.getPaymentMethod() != PaymentMethod.INTERNAL) {
            throw new IllegalStateException("Payment redirect is available only for INTERNAL orders");
        }
        orderService.assertPending(order);

        String sessionId = UUID.randomUUID().toString();
        String redirectUrl = "https://google.com"
                + "?session=" + sessionId
                + "&orderId=" + order.getId()
                + "&amount=" + order.getTotalPrice()
                + "&currency=" + order.getCurrency()
                + "&returnUrl=/callback";
        return new PaymentRedirectResponse(redirectUrl, sessionId);
    }
}
