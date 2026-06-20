package ru.gigasigma.blpscrud.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.controller.dto.request.PaymentCallbackRequest;
import ru.gigasigma.blpscrud.service.PaymentCallbackProcessingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCallbackListener {

    private final PaymentCallbackProcessingService callbackProcessingService;

    @PostConstruct
    public void init() {
        log.info("=== PaymentCallbackListener bean created ===");
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new SimpleMessageConverter());
        return factory;
    }

    @JmsListener(destination = "payment.callback.queue")
    public void handleCallback(
            String json
    ) throws JsonProcessingException {
        var req =
                new ObjectMapper().readValue(json, PaymentCallbackRequest.class);
        log.info("{}", req);
        callbackProcessingService.handleCallback(req);
    }
}
