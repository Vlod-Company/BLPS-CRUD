package ru.gigasigma.blpscrud.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.controller.dto.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.controller.dto.StartPurchaseRequest;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.service.*;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.util.PaymentServiceFactory;
import ru.gigasigma.blpscrud.util.PurchaseUtil;


@Service
@RequiredArgsConstructor
public class InternalPurchaseServiceImpl implements InternalPurchaseService {

    private final OrderRepository orderRepository;
    private final PaymentServiceFactory paymentServiceFactory;
    private final AirlineBookingService airlineBookingService;
    private final TicketDeliveryService ticketDeliveryService;
    private final OrderService orderService;
    private final PurchaseUtil purchaseUtil;

    @Override
    @Transactional
    public PaymentRedirectResponse startInternalPurchase(StartPurchaseRequest request) {
        Order order = orderService.createOrderWithTicket(request, PaymentMethod.INTERNAL, null);
        return paymentServiceFactory.getPaymentService(request.provider()).generatePaymentRedirectLink(order.getId());
    }

    @Override
    @Transactional
    public WorkflowResult handlePaymentCallback(PaymentCallbackRequest request) {
        Order order = orderService.getOrder(request.orderId());
        if (order.getPaymentMethod() != PaymentMethod.INTERNAL) {
            throw new IllegalStateException("Order is not INTERNAL");
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Canceled order cannot be paid");
        }
        if (!request.success()) {
            String reason = request.failureReason() == null || request.failureReason().isBlank()
                    ? "Payment failed"
                    : request.failureReason();
            return purchaseUtil.toResult(order, reason);
        }
        if (order.getStatus() == OrderStatus.PAID) {
            return purchaseUtil.toResult(order, "Order already paid");
        }
        orderService.assertPending(order);
        if (request.paidAmount() != null && request.paidAmount().compareTo(order.getTotalPrice()) < 0) {
            throw new IllegalStateException("Paid amount is less than order total");
        }

        Ticket ticket = orderService.getOrderTicket(order.getId());
        airlineBookingService.issueTicket(order, ticket);
        order.setStatus(OrderStatus.PAID);
        Order saved = orderRepository.save(order);
        ticketDeliveryService.sendTicket(saved, ticket);
        return purchaseUtil.toResult(saved, "Payment callback success. externalPaymentId=" + request.externalPaymentId());
    }
}