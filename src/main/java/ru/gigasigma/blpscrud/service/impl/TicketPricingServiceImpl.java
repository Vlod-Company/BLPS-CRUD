package ru.gigasigma.blpscrud.service.impl;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gigasigma.blpscrud.enums.SeatClass;
import ru.gigasigma.blpscrud.service.TicketPricingService;

@Service
@RequiredArgsConstructor
public class TicketPricingServiceImpl implements TicketPricingService {

    @Value("${pricing.business-multiplier:1.40}")
    private BigDecimal businessMultiplier;

    @Value("${pricing.baggage-fee:0}")
    private BigDecimal baggageFee;

    @Override
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
