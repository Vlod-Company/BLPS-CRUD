package ru.gigasigma.blpscrud.camunda;

import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import ru.gigasigma.blpscrud.controller.dto.request.ExternalBookingCallbackRequest;
import ru.gigasigma.blpscrud.entity.Flight;
import ru.gigasigma.blpscrud.enums.SeatClass;
import ru.gigasigma.blpscrud.repository.FlightRepository;
import ru.gigasigma.blpscrud.repository.OrderRepository;
import ru.gigasigma.blpscrud.repository.TicketRepository;
import ru.gigasigma.blpscrud.security.XmlAccount;
import ru.gigasigma.blpscrud.security.XmlUserStore;
import ru.gigasigma.blpscrud.service.CurrentUserService;
import ru.gigasigma.blpscrud.service.TicketPricingService;
import ru.gigasigma.blpscrud.service.crm.LaxoCrmExportService;
import ru.gigasigma.blpscrud.service.externalAirlineLogic.impl.ExternalPurchaseServiceImpl;
import ru.gigasigma.blpscrud.service.flightSync.FlightSyncService;
import ru.gigasigma.blpscrud.service.ticketDelivery.TicketDeliveryService;
import ru.gigasigma.blpscrud.util.PurchaseUtil;

class ExternalBookingDeliveryTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void processDefersEmailButLegacyStillSendsIt(boolean process) {
        var users = mock(XmlUserStore.class);
        var flights = mock(FlightRepository.class);
        var orders = mock(OrderRepository.class);
        var tickets = mock(TicketRepository.class);
        var pricing = mock(TicketPricingService.class);
        var sync = mock(FlightSyncService.class);
        var delivery = mock(TicketDeliveryService.class);
        var transactions = mock(PlatformTransactionManager.class);
        var crm = mock(LaxoCrmExportService.class);
        var flight = new Flight();
        flight.setId(2L);
        flight.setAvailableSeats(10);
        flight.setBasePrice(BigDecimal.TEN);
        when(users.findById(1L)).thenReturn(Optional.of(new XmlAccount(1L, "alice", "hash", "ROLE_USER", "Alice")));
        when(sync.refreshFlightForPurchase(2L)).thenReturn(flight);
        when(pricing.calculateTotalPrice(BigDecimal.TEN, SeatClass.ECONOMY, false)).thenReturn(BigDecimal.TEN);
        when(orders.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tickets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var transaction = new SimpleTransactionStatus();
        when(transactions.getTransaction(any())).thenReturn(transaction);
        var service = new ExternalPurchaseServiceImpl(users, flights, orders, tickets, pricing, sync,
                delivery, mock(PurchaseUtil.class), mock(CurrentUserService.class), transactions, crm);
        var request = new ExternalBookingCallbackRequest(1L, 2L, "RUB", "12A", SeatClass.ECONOMY,
                false, "Ivan Ivanov", "1234 567890", "ivan@example.com", "+79991234567",
                "payment", "booking", "ticket", BigDecimal.TEN);

        if (process) {
            service.completeExternalBookingForProcess(request);
        } else {
            service.completeExternalBooking(request);
        }

        verify(orders).save(any());
        verify(tickets).save(any());
        verify(transactions).commit(transaction);
        verify(delivery, times(process ? 0 : 1)).sendTicket(any(), any());
        verify(crm).exportSuccessfulPurchase(any(), any());
    }
}
