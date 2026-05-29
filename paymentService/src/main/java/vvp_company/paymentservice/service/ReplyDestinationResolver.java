package vvp_company.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vvp_company.paymentservice.cfg.ReplyDestinationProperties;

@Service
@RequiredArgsConstructor
public class ReplyDestinationResolver {

    private final ReplyDestinationProperties properties;

    public String resolve(String replyTo) {

        String destination = properties
                .getReplyDestinations()
                .get(replyTo);

        if (destination == null) {
            throw new IllegalArgumentException(
                    "Unknown reply destination: " + replyTo
            );
        }

        return destination;
    }
}
