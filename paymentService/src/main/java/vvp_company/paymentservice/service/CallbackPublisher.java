package vvp_company.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import vvp_company.paymentservice.dto.PaymentCallbackRequest;

@Service
@RequiredArgsConstructor
public class CallbackPublisher {

    private final JmsTemplate jmsTemplate;

    public void publish(
            String destination,
            String sessionId,
            PaymentCallbackRequest payload
    ) {
        jmsTemplate.convertAndSend(
                destination,
                payload,
                message -> {
                    message.setJMSCorrelationID(sessionId);
                    return message;
                }
        );
    }
}