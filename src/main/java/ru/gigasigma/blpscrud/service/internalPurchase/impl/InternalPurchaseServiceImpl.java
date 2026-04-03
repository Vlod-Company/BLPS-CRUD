package ru.gigasigma.blpscrud.service.internalPurchase.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.controller.dto.request.StartPurchaseRequest;
import ru.gigasigma.blpscrud.controller.dto.response.PaymentRedirectResponse;
import ru.gigasigma.blpscrud.entity.Order;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.enums.OrderStatus;
import ru.gigasigma.blpscrud.enums.PaymentMethod;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.service.CurrentUserService;
import ru.gigasigma.blpscrud.service.OrderService;
import ru.gigasigma.blpscrud.service.dto.WorkflowResult;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.AirlineBookingService;
import ru.gigasigma.blpscrud.service.internalPurchase.InternalPurchaseService;
import ru.gigasigma.blpscrud.service.ticketDelivery.TicketDeliveryService;
import ru.gigasigma.blpscrud.util.PurchaseUtil;

@Service
@RequiredArgsConstructor
public class InternalPurchaseServiceImpl implements InternalPurchaseService {

    private final OrderRepository orderRepository;
    private final AirlineBookingService airlineBookingService;
    private final TicketDeliveryService ticketDeliveryService;
    private final OrderService orderService;
    private final PurchaseUtil purchaseUtil;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public PaymentRedirectResponse startInternalPurchase(StartPurchaseRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        Order order = orderService.createOrderWithTicket(request, userId, PaymentMethod.INTERNAL, null);
        PaymentRedirectResponse redirect = generatePaymentRedirectLink(order.getId());

        if (redirect == null || redirect.redirectUrl() == null || redirect.redirectUrl().isBlank()) {
            throw new IllegalStateException("Payment provider did not return redirect URL");
        }
        return redirect;
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
