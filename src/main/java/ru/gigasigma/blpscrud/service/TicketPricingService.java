package ru.gigasigma.blpscrud.service;

import java.math.BigDecimal;
import ru.gigasigma.blpscrud.enums.SeatClass;

public interface TicketPricingService {
    BigDecimal calculateTotalPrice(BigDecimal basePrice, SeatClass seatClass, boolean hasBaggage);
}
