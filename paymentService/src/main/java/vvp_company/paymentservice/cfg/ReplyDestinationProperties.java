package vvp_company.paymentservice.cfg;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("app")
@Getter
@Setter
public class ReplyDestinationProperties {

    private Map<String, String> replyDestinations = new HashMap<>();
}
