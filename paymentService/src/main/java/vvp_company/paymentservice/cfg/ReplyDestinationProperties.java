package vvp_company.paymentservice.cfg;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("app")
@Data
public class ReplyDestinationProperties {

    private Map<String, String> replyDestinations = new HashMap<>();
}
