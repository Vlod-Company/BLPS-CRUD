package ru.gigasigma.blpscrud.camunda.delegate;

import static ru.gigasigma.blpscrud.camunda.CamundaVariables.longValue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import ru.gigasigma.blpscrud.service.FlightService;

@Slf4j
@Component("checkSeatsDelegate")
@RequiredArgsConstructor
public class CheckSeatsDelegate implements JavaDelegate {

    private final FlightService flightService;

    @Override
    public void execute(DelegateExecution execution) {
        Long flightId = longValue(execution, "flightId");
        if (flightId == null) {
            execution.setVariable("seatsAvailable", true);
            return;
        }
        var flight = flightService.getById(flightId);
        if (flight.getAvailableSeats() < 0) {
            throw new BpmnError("SEAT_UNAVAILABLE", "No seats available");
        }
        execution.setVariable("seatsAvailable", true);
        log.info("Camunda seat check completed. flightId={}, availableSeats={}", flightId, flight.getAvailableSeats());
    }
}
