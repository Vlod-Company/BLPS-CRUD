package one.laxo.crm.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrmDealRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private BigDecimal amount;
    private String currency;
    private Long externalOrderId;
    private String orderStatus;
    private String paymentMethod;
    private String flightNumber;
    private String route;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String seatNumber;
    private String seatClass;
    private Boolean hasBaggage;
    private String airlineName;
    private String contactId;
}
