package vvp_company.paymentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    ) throws JsonProcessingException {
        jmsTemplate.convertAndSend(
                destination,
                new ObjectMapper().writeValueAsString(payload),
                message -> {
                    message.setJMSCorrelationID(sessionId);
                    return message;
                }
        );
    }
}