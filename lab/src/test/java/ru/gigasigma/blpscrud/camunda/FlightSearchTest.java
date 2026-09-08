package ru.gigasigma.blpscrud.camunda;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import ru.gigasigma.blpscrud.camunda.delegate.ShowFlightsDelegate;
import ru.gigasigma.blpscrud.service.FlightService;

class FlightSearchTest {
    @Test
    void newSearchIgnoresPreviouslySelectedFlightAndClearsDecision() {
        var flights = mock(FlightService.class);
        var execution = mock(DelegateExecution.class);
        var date = LocalDate.now().plusDays(5);
        when(execution.getVariable("flightId")).thenReturn(17L);
        when(execution.getVariable("from")).thenReturn("LED");
        when(execution.getVariable("to")).thenReturn("SVO");
        when(execution.getVariable("date")).thenReturn(date.toString());
        when(flights.search(eq("LED"), eq("SVO"), any(), any(), eq(1))).thenReturn(List.of());

        new ShowFlightsDelegate(flights, new ObjectMapper()).execute(execution);

        verify(flights, never()).getById(any());
        verify(flights).search("LED", "SVO", date.atStartOfDay(),
                date.plusDays(1).atStartOfDay().minusSeconds(1), 1);
        verify(execution).removeVariable("flightId");
        verify(execution).removeVariable("hasSuitableFlight");
        verify(execution).setVariable("availableFlights", List.of());
        verify(execution).setVariable("hasAvailableFlights", false);
    }

    @Test
    void incompleteSearchDoesNotLeavePreviousResults() {
        var flights = mock(FlightService.class);
        var execution = mock(DelegateExecution.class);

        new ShowFlightsDelegate(flights, new ObjectMapper()).execute(execution);

        verifyNoInteractions(flights);
        verify(execution).setVariable("availableFlights", List.of());
        verify(execution).setVariable("availableFlightsSummary", "");
        verify(execution).setVariable("availableFlightCount", 0);
    }
}
