package one.laxo.crm.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrmPurchaseExportRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long userId;
    private LocalDateTime createdAt;
    private BigDecimal totalPrice;
    private String currency;
    private String orderStatus;
    private String paymentMethod;
    private String externalLink;
    private Long ticketId;
    private String seatNumber;
    private String seatClass;
    private Boolean hasBaggage;
    private String passengerName;
    private String passengerPassport;
    private Long flightId;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String aircraftType;
    private BigDecimal basePrice;
    private String airlineName;
    private String airlineIataCode;
    private String airlineCountry;
    private String airlineWebsiteUrl;
}
