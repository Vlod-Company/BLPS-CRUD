package vvp_company.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vvp_company.paymentservice.dto.PaymentCallbackRequest;
import vvp_company.paymentservice.dto.PaymentPageRequest;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CallbackPublisher callbackPublisher;
    private final ReplyDestinationResolver destinationResolver;

    public boolean processPayment(PaymentPageRequest request) {

        var success = new Random().nextInt(10) != 1;

        var callback = new PaymentCallbackRequest(
                request.orderId(),
                success,
                UUID.randomUUID().toString(),
                success ? null : "Payment declined",
                success ? request.amount() : BigDecimal.ZERO
        );

        var destination = destinationResolver
                .resolve(request.replyTo());

        callbackPublisher.publish(
                destination,
                request.session(),
                callback
        );

        return success;
    }
}