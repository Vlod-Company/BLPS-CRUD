package ru.gigasigma.blpscrud.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.enums.SeatClass;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TicketPricingService {

    @Value("${pricing.business-multiplier:1.40}")
    private BigDecimal businessMultiplier;

    @Value("${pricing.baggage-fee:0}")
    private BigDecimal baggageFee;

    public BigDecimal calculateTotalPrice(BigDecimal basePrice, SeatClass seatClass, boolean hasBaggage) {
        BigDecimal price = basePrice;
        if (seatClass == SeatClass.BUSINESS) {
            price = price.multiply(businessMultiplier);
        }
        if (hasBaggage) {
            price = price.add(baggageFee);
        }
        return price;
    }
}
