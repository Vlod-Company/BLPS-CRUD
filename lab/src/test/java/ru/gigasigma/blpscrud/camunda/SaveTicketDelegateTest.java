package ru.gigasigma.blpscrud.camunda;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import ru.gigasigma.blpscrud.camunda.delegate.SaveTicketDelegate;
import ru.gigasigma.blpscrud.entity.Ticket;
import ru.gigasigma.blpscrud.service.OrderService;

class SaveTicketDelegateTest {
    @Test
    void confirmsOnlyPersistedTicket() {
        var orders = mock(OrderService.class);
        var execution = mock(DelegateExecution.class);
        var delegate = new SaveTicketDelegate(orders);
        assertThatThrownBy(() -> delegate.execute(execution)).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(orders);

        when(execution.getVariable("orderId")).thenReturn(1L);
        assertThatThrownBy(() -> delegate.execute(execution)).isInstanceOf(IllegalStateException.class);
        verify(execution, never()).setVariable("ticketSaved", true);

        var ticket = new Ticket();
        ticket.setId(2L);
        when(orders.getOrderTicket(1L)).thenReturn(ticket);
        delegate.execute(execution);
        verify(execution).setVariable("ticketId", 2L);
        verify(execution).setVariable("ticketSaved", true);
    }
}
